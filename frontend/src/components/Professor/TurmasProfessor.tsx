import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import HttpService from '../../services/HttpService';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { extractApiMessage } from '../../utils/apiError';
import './TurmasProfessor.css';

interface TurmaResumo {
  id: number;
  modalidade: string;
  horario: string;
  sala?: string;
  dias?: string[];
  horaInicio?: string;
  horaFim?: string;
  totalAlunos?: number;
}

interface AlunoTurma {
  cpf: string;
  nome: string;
  cpfMascarado: string;
  telefoneMascarado: string;
}

const DIAS_LABEL: Record<string, string> = {
  'segunda-feira': 'Seg',
  'terça-feira': 'Ter',
  'quarta-feira': 'Qua',
  'quinta-feira': 'Qui',
  'sexta-feira': 'Sex',
  'sábado': 'Sáb',
  'domingo': 'Dom',
};

const maskCPF = (v: string) =>
  v.replace(/\D/g, '')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
    .slice(0, 14);

const formatarDias = (dias?: string[]) =>
  dias?.length ? dias.map((d) => DIAS_LABEL[d] || d).join(', ') : '—';

const descricaoTurma = (t: TurmaResumo) => {
  const horario = t.horaInicio && t.horaFim ? `${t.horaInicio}–${t.horaFim}` : t.horario;
  const sala = t.sala || 'Sala não informada';
  return `${sala} · ${horario} · ${formatarDias(t.dias)}`;
};

const TurmasProfessor: React.FC = () => {
  const [turmas, setTurmas] = useState<TurmaResumo[]>([]);
  const [turmaId, setTurmaId] = useState<number | null>(null);
  const [alunos, setAlunos] = useState<AlunoTurma[]>([]);
  const [cpfNovo, setCpfNovo] = useState('');
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const turmaSelecionada = turmas.find((t) => t.id === turmaId) || null;

  const carregarTurmas = useCallback(() => {
    HttpService.minhasTurmasProfessor()
      .then((r) => {
        const lista = r.data || [];
        setTurmas(lista);
        if (lista.length > 0 && !turmaId) {
          setTurmaId(lista[0].id);
        }
      })
      .catch(() => setTurmas([]));
  }, [turmaId]);

  const carregarAlunos = (id: number) => {
    HttpService.alunosDaTurma(id)
      .then((r) => setAlunos(r.data || []))
      .catch(() => setAlunos([]));
  };

  useEffect(() => {
    carregarTurmas();
  }, [carregarTurmas]);

  useEffect(() => {
    if (turmaId) carregarAlunos(turmaId);
    else setAlunos([]);
  }, [turmaId]);

  const adicionarAluno = async () => {
    if (!turmaId) return;
    const cpf = cpfNovo.replace(/\D/g, '');
    if (cpf.length < 11) {
      setModal({ open: true, success: false, message: 'Informe um CPF válido com 11 dígitos.' });
      return;
    }
    setLoading(true);
    try {
      await HttpService.adicionarAlunoTurmaProfessor(turmaId, cpf);
      setCpfNovo('');
      carregarAlunos(turmaId);
      carregarTurmas();
      setModal({ open: true, success: true, message: 'Aluno adicionado à turma.' });
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Não foi possível adicionar o aluno.') });
    } finally {
      setLoading(false);
    }
  };

  const removerAluno = async (cpf: string) => {
    if (!turmaId || !window.confirm('Remover este aluno da turma?')) return;
    try {
      await HttpService.removerAlunoTurmaProfessor(turmaId, cpf);
      carregarAlunos(turmaId);
      carregarTurmas();
      setModal({ open: true, success: true, message: 'Aluno removido da turma.' });
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  return (
    <PageShell title="Minhas turmas" subtitle="Gerencie alunos das turmas vinculadas ao seu perfil">
      {turmas.length === 0 ? (
        <div className="card"><p>Nenhuma turma vinculada ao seu CPF.</p></div>
      ) : (
        <div className="turmas-professor">
          <div className="turmas-professor__lista">
            {turmas.map((t) => (
              <button
                key={t.id}
                type="button"
                className={`turmas-professor__card${turmaId === t.id ? ' turmas-professor__card--ativa' : ''}`}
                onClick={() => setTurmaId(t.id)}
              >
                <h3>{t.modalidade}</h3>
                <p className="turmas-professor__meta">{descricaoTurma(t)}</p>
                <p className="turmas-professor__meta">{t.totalAlunos ?? 0} aluno(s)</p>
              </button>
            ))}
          </div>

          {turmaSelecionada && (
            <div className="card">
              <div className="turmas-professor__detalhe-header">
                <div>
                  <h3 style={{ margin: 0 }}>{turmaSelecionada.modalidade}</h3>
                  <p className="turmas-professor__meta" style={{ margin: '0.25rem 0 0' }}>
                    {descricaoTurma(turmaSelecionada)}
                  </p>
                </div>
                <Link className="btn-secondary" to={`/arealogada/professor/presenca?turma=${turmaSelecionada.id}`}>
                  Presença
                </Link>
              </div>

              <div className="turmas-professor__add-row">
                <input
                  type="text"
                  placeholder="CPF do aluno"
                  value={cpfNovo}
                  onChange={(e) => setCpfNovo(maskCPF(e.target.value))}
                />
                <button type="button" className="btn-primary" disabled={loading} onClick={adicionarAluno}>
                  Adicionar aluno
                </button>
              </div>

              <div className="table-wrap">
                <table className="cert-form__table" style={{ width: '100%' }}>
                  <thead>
                    <tr>
                      <th>Nome</th>
                      <th>CPF</th>
                      <th>Telefone</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {alunos.map((a) => (
                      <tr key={a.cpf}>
                        <td>{a.nome}</td>
                        <td>{a.cpfMascarado}</td>
                        <td>{a.telefoneMascarado || '—'}</td>
                        <td>
                          <button type="button" className="btn-sm btn-sm--remove" onClick={() => removerAluno(a.cpf)}>
                            Remover
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {alunos.length === 0 && <p className="field-hint">Nenhum aluno nesta turma.</p>}
              </div>
            </div>
          )}
        </div>
      )}

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default TurmasProfessor;

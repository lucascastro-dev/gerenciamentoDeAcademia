import { useEffect, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import DiasSemanaMultiSelect from '../common/DiasSemanaMultiSelect';
import { carregarSessao, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import '../../theme/turmas.css';

interface Professor {
  cpf: string;
  nome: string;
}

interface SalaOpt {
  id: number;
  nome: string;
}

interface TurmaItem {
  id: number;
  modalidade: string;
  horario: string;
  sala?: string;
  dias?: string[];
  professor?: { cpf: string; nome: string };
}

interface Props {
  modo?: 'consulta' | 'gerenciar';
}

function montarHorarioApi(horaInicio: string, horaFim: string): string {
  return `${horaInicio}-${horaFim}`;
}

function validarIntervalo(horaInicio: string, horaFim: string): string | null {
  if (!horaInicio || !horaFim) return 'Informe o horário de início e de término.';
  if (horaFim <= horaInicio) return 'O horário de término deve ser depois do início.';
  return null;
}

const GerenciarTurmas: React.FC<Props> = ({ modo }) => {
  const sessao = carregarSessao();
  const podeGerenciar = possuiPermissao(sessao, 'turma:gerenciar');
  const somenteCadastro = modo === 'gerenciar';

  const [turmas, setTurmas] = useState<TurmaItem[]>([]);
  const [professores, setProfessores] = useState<Professor[]>([]);
  const [salas, setSalas] = useState<SalaOpt[]>([]);
  const [horaInicio, setHoraInicio] = useState('18:00');
  const [horaFim, setHoraFim] = useState('19:30');
  const [sala, setSala] = useState('');
  const [modalidade, setModalidade] = useState('');
  const [cpfProfessor, setCpfProfessor] = useState('');
  const [diasSel, setDiasSel] = useState<string[]>([]);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const [vinculoProfTurma, setVinculoProfTurma] = useState<Record<number, string>>({});

  const MODALIDADE_MATRICULA = 'Matrícula institucional';

  const reload = () => HttpService.listarTurmas().then((r) => {
    const lista = (r.data || []).filter((t: TurmaItem) => t.modalidade !== MODALIDADE_MATRICULA);
    setTurmas(lista);
  });

  useEffect(() => {
    reload();
    const precisaProfessoresSalas = (somenteCadastro || podeGerenciar) && sessao?.vinculo;
    if (precisaProfessoresSalas) {
      HttpService.professoresInstituicao(sessao.vinculo)
        .then((r) => setProfessores(r.data))
        .catch(() => setProfessores([]));
      HttpService.programacaoListarSalas(sessao.vinculo)
        .then((r) => setSalas(r.data))
        .catch(() => setSalas([]));
    }
  }, [somenteCadastro, podeGerenciar, sessao?.vinculo]);

  const criar = async () => {
    const erroHorario = validarIntervalo(horaInicio, horaFim);
    if (erroHorario) {
      setModal({ open: true, success: false, message: erroHorario });
      return;
    }
    if (!modalidade.trim()) {
      setModal({ open: true, success: false, message: 'Informe a modalidade.' });
      return;
    }
    if (diasSel.length === 0) {
      setModal({ open: true, success: false, message: 'Selecione ao menos um dia da semana.' });
      return;
    }
    try {
      await HttpService.montarTurma({
        instituicaoId: Number(sessao?.vinculo),
        horario: montarHorarioApi(horaInicio, horaFim),
        sala: sala || null,
        modalidade,
        cpfProfessor: cpfProfessor || null,
        dias: diasSel,
        alunos: [],
      });
      setModal({ open: true, success: true, message: 'Turma criada com sucesso.' });
      setHoraInicio('18:00');
      setHoraFim('19:30');
      setSala('');
      setModalidade('');
      setCpfProfessor('');
      setDiasSel([]);
      reload();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao criar turma.') });
    }
  };

  const vincularProfessor = async (turmaId: number) => {
    try {
      await HttpService.vincularProfessorTurma(turmaId, vinculoProfTurma[turmaId] || '');
      setModal({ open: true, success: true, message: 'Professor atualizado na turma.' });
      reload();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  const excluir = async (id: number) => {
    if (!window.confirm('Excluir esta turma?')) return;
    try {
      await HttpService.excluirTurma(id);
      setModal({ open: true, success: true, message: 'Turma excluída.' });
      reload();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  return (
    <PageShell
      title={somenteCadastro ? 'Cadastrar turma' : 'Consultar turmas'}
      subtitle={somenteCadastro
        ? 'Cadastre novas turmas com horário, sala e professor'
        : 'Lista das turmas da instituição — altere professor ou exclua quando necessário'}
    >
      <div className={`turmas-layout ${somenteCadastro ? 'turmas-layout--gerenciar' : ''}`}>
        {somenteCadastro && (
          <div className="card turmas-form-card turmas-form-section">
            <h3>Nova turma</h3>
            <p className="field-hint">Professor opcional; pode vincular depois na lista.</p>

            <div className="form-grid">
              <div>
                <label>Modalidade</label>
                <input value={modalidade} onChange={(e) => setModalidade(e.target.value)} placeholder="Ex.: Judô adulto" />
              </div>
              <div>
                <label>Sala</label>
                <select value={sala} onChange={(e) => setSala(e.target.value)}>
                  <option value="">Sem sala definida</option>
                  {salas.map((s) => (
                    <option key={s.id} value={s.nome}>{s.nome}</option>
                  ))}
                </select>
              </div>
              <div>
                <label>Professor (opcional)</label>
                <select value={cpfProfessor} onChange={(e) => setCpfProfessor(e.target.value)}>
                  <option value="">Sem professor no momento</option>
                  {professores.map((p) => (
                    <option key={p.cpf} value={p.cpf}>{p.nome}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="horario-range">
              <label>Horário da aula</label>
              <div className="turmas-horario-row">
                <div>
                  <span className="horario-range__legend">Início</span>
                  <input type="time" value={horaInicio} onChange={(e) => setHoraInicio(e.target.value)} />
                </div>
                <span className="horario-range__sep">até</span>
                <div>
                  <span className="horario-range__legend">Término</span>
                  <input type="time" value={horaFim} onChange={(e) => setHoraFim(e.target.value)} />
                </div>
              </div>
              <p className="field-hint" style={{ marginBottom: 0 }}>
                Registro: <strong>{montarHorarioApi(horaInicio, horaFim)}</strong>
              </p>
            </div>

            <DiasSemanaMultiSelect value={diasSel} onChange={setDiasSel} />

            <div className="form-actions form-actions--compact">
              <button type="button" className="btn-primary" onClick={criar}>Criar turma</button>
            </div>
          </div>
        )}

        {!somenteCadastro && (
        <div className="card turmas-list-card" style={{ gridColumn: '1 / -1' }}>
          <h3>Turmas ({turmas.length})</h3>
          {turmas.length === 0 && <p className="field-hint">Nenhuma turma cadastrada.</p>}
          {turmas.map((t) => (
            <div key={t.id} className="turmas-item">
              <strong>{t.modalidade}</strong>
              <div className="turmas-item__meta">
                <span className="turmas-badge">{t.horario}</span>
                {t.sala && <span className="turmas-badge">{t.sala}</span>}
                {t.dias?.map((d) => <span key={d} className="turmas-badge">{d}</span>)}
                <span className="turmas-badge">Prof.: {t.professor?.nome || 'Não vinculado'}</span>
              </div>
              {podeGerenciar && (
                <>
                  <div className="form-grid" style={{ marginTop: '0.75rem', maxWidth: 420 }}>
                    <div>
                      <label>Vincular professor</label>
                      <select
                        value={vinculoProfTurma[t.id] ?? t.professor?.cpf ?? ''}
                        onChange={(e) => setVinculoProfTurma((prev) => ({ ...prev, [t.id]: e.target.value }))}
                      >
                        <option value="">Remover professor</option>
                        {professores.map((p) => (
                          <option key={p.cpf} value={p.cpf}>{p.nome}</option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div className="form-actions form-actions--compact">
                    <button type="button" className="btn-secondary" onClick={() => vincularProfessor(t.id)}>Salvar professor</button>
                    <button type="button" className="btn-danger" onClick={() => excluir(t.id)}>Excluir</button>
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
        )}
      </div>

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default GerenciarTurmas;

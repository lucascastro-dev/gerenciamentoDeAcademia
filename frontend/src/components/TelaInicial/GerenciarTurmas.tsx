import { useEffect, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { carregarSessao, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';

const DIAS = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'];

interface Professor {
  cpf: string;
  nome: string;
}

const GerenciarTurmas: React.FC = () => {
  const sessao = carregarSessao();
  const podeGerenciar = possuiPermissao(sessao, 'turma:gerenciar');
  const [turmas, setTurmas] = useState<any[]>([]);
  const [professores, setProfessores] = useState<Professor[]>([]);
  const [horario, setHorario] = useState('');
  const [modalidade, setModalidade] = useState('');
  const [cpfProfessor, setCpfProfessor] = useState('');
  const [diasSel, setDiasSel] = useState<string[]>([]);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const [vinculoProfTurma, setVinculoProfTurma] = useState<Record<number, string>>({});

  const reload = () => HttpService.listarTurmas().then((r) => setTurmas(r.data));

  useEffect(() => {
    reload();
    if (podeGerenciar && sessao?.vinculo) {
      HttpService.professoresInstituicao(sessao.vinculo)
        .then((r) => setProfessores(r.data))
        .catch(() => setProfessores([]));
    }
  }, [podeGerenciar, sessao?.vinculo]);

  const toggleDia = (d: string) => {
    setDiasSel((prev) => (prev.includes(d) ? prev.filter((x) => x !== d) : [...prev, d]));
  };

  const criar = async () => {
    try {
      await HttpService.montarTurma({
        horario,
        modalidade,
        cpfProfessor: cpfProfessor || null,
        dias: diasSel,
        alunos: [],
      });
      setModal({ open: true, success: true, message: 'Turma criada com sucesso.' });
      setHorario('');
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
    try {
      await HttpService.excluirTurma(id);
      setModal({ open: true, success: true, message: 'Turma excluída.' });
      reload();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  return (
    <PageShell title={podeGerenciar ? 'Gestão de turmas' : 'Consulta de turmas'}>
      {podeGerenciar && (
      <div className="card" style={{ marginBottom: '1rem' }}>
        <h3 style={{ marginTop: 0 }}>Nova turma</h3>
        <p className="field-hint">O professor é opcional; você pode vincular depois.</p>
        <div className="form-grid">
          <div><label>Modalidade</label><input value={modalidade} onChange={(e) => setModalidade(e.target.value)} /></div>
          <div><label>Horário</label><input value={horario} onChange={(e) => setHorario(e.target.value)} placeholder="19:00-20:00" /></div>
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
        <div style={{ marginTop: '0.75rem' }}>
          {DIAS.map((d) => (
            <label key={d} style={{ marginRight: 12 }}>
              <input type="checkbox" checked={diasSel.includes(d)} onChange={() => toggleDia(d)} /> {d}
            </label>
          ))}
        </div>
        <div className="form-actions form-actions--compact">
          <button type="button" className="btn-primary" onClick={criar}>Criar turma</button>
        </div>
      </div>
      )}

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Turmas ({turmas.length})</h3>
        {turmas.map((t) => (
          <div key={t.id} className="turma-item">
            <div>
              <strong>{t.modalidade}</strong> — {t.horario}
              <div className="field-hint">
                Prof.: {t.professor?.nome || 'Não vinculado'}
              </div>
            </div>
            {podeGerenciar && (
              <>
                <div className="form-grid" style={{ marginTop: '0.5rem', maxWidth: 480 }}>
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

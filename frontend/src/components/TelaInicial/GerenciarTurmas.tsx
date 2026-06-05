import { useEffect, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import DiasSemanaMultiSelect from '../common/DiasSemanaMultiSelect';
import { carregarSessao, isModoPlataforma, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { mapInstituicoesApi } from '../../utils/instituicao';
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
  instituicao?: { id: number; razaoSocial?: string };
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

function parseHorario(horario: string): { inicio: string; fim: string } {
  const partes = (horario || '').split('-').map((p) => p.trim());
  return { inicio: partes[0] || '18:00', fim: partes[1] || '19:30' };
}

const GerenciarTurmas: React.FC<Props> = ({ modo }) => {
  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);
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
  const [professoresPorTurma, setProfessoresPorTurma] = useState<Record<number, Professor[]>>({});
  const [salasPorTurma, setSalasPorTurma] = useState<Record<number, SalaOpt[]>>({});
  const [turmaEmEdicao, setTurmaEmEdicao] = useState<number | null>(null);
  const [editModalidade, setEditModalidade] = useState('');
  const [editHoraInicio, setEditHoraInicio] = useState('18:00');
  const [editHoraFim, setEditHoraFim] = useState('19:30');
  const [editSala, setEditSala] = useState('');
  const [editDias, setEditDias] = useState<string[]>([]);
  const vinculoInst = sessao?.vinculo && sessao.vinculo !== '0' ? sessao.vinculo : '';
  const [instituicaoCadastro, setInstituicaoCadastro] = useState(vinculoInst);
  const [instituicoes, setInstituicoes] = useState<Array<{ id: number; razaoSocial: string }>>([]);
  const [filtroInstituicao, setFiltroInstituicao] = useState('');
  const [filtroProfessor, setFiltroProfessor] = useState('');
  const [filtroDias, setFiltroDias] = useState<string[]>([]);
  const [professoresFiltro, setProfessoresFiltro] = useState<Professor[]>([]);

  const MODALIDADE_MATRICULA = 'Matrícula institucional';
  const instituicaoIdAtiva = master && somenteCadastro ? instituicaoCadastro : vinculoInst;
  const instituicaoIdFiltroProf = master && !somenteCadastro
    ? (filtroInstituicao || '')
    : vinculoInst;

  const reload = () => {
    const params: { instituicaoId?: number; professorCpf?: string; dias?: string[] } = {};
    if (master && filtroInstituicao) params.instituicaoId = Number(filtroInstituicao);
    if (filtroProfessor) params.professorCpf = filtroProfessor;
    if (filtroDias.length > 0) params.dias = filtroDias;
    const usaFiltroBackend = !master || !!filtroInstituicao || !!filtroProfessor || filtroDias.length > 0;
    return HttpService.listarTurmas(usaFiltroBackend ? params : undefined).then((r) => {
      const lista = (r.data || []).filter((t: TurmaItem) => t.modalidade !== MODALIDADE_MATRICULA);
      setTurmas(lista);
    });
  };

  const instituicaoIdTurma = (turma: TurmaItem) =>
    turma.instituicao?.id ?? (sessao?.vinculo && sessao.vinculo !== '0' ? Number(sessao.vinculo) : null);

  const carregarProfessoresTurma = async (turma: TurmaItem) => {
    const instId = instituicaoIdTurma(turma);
    if (!instId) return;
    try {
      const r = await HttpService.professoresInstituicao(instId);
      setProfessoresPorTurma((prev) => ({ ...prev, [turma.id]: r.data }));
    } catch {
      setProfessoresPorTurma((prev) => ({ ...prev, [turma.id]: [] }));
    }
  };

  const carregarSalasTurma = async (turma: TurmaItem) => {
    const instId = instituicaoIdTurma(turma);
    if (!instId) return;
    try {
      const r = await HttpService.programacaoListarSalas(instId);
      setSalasPorTurma((prev) => ({ ...prev, [turma.id]: r.data }));
    } catch {
      setSalasPorTurma((prev) => ({ ...prev, [turma.id]: [] }));
    }
  };

  const iniciarEdicao = (t: TurmaItem) => {
    const { inicio, fim } = parseHorario(t.horario);
    setTurmaEmEdicao(t.id);
    setEditModalidade(t.modalidade);
    setEditHoraInicio(inicio);
    setEditHoraFim(fim);
    setEditSala(t.sala || '');
    setEditDias(t.dias || []);
    if (master) {
      carregarSalasTurma(t);
    }
  };

  const cancelarEdicao = () => setTurmaEmEdicao(null);

  const salvarEdicaoTurma = async (turmaId: number) => {
    const erroHorario = validarIntervalo(editHoraInicio, editHoraFim);
    if (erroHorario) {
      setModal({ open: true, success: false, message: erroHorario });
      return;
    }
    if (!editModalidade.trim()) {
      setModal({ open: true, success: false, message: 'Informe a modalidade.' });
      return;
    }
    if (editDias.length === 0) {
      setModal({ open: true, success: false, message: 'Selecione ao menos um dia da semana.' });
      return;
    }
    try {
      await HttpService.alterarTurma({
        id: turmaId,
        modalidade: editModalidade,
        horario: montarHorarioApi(editHoraInicio, editHoraFim),
        sala: editSala || '',
        dias: editDias,
      });
      setModal({ open: true, success: true, message: 'Turma atualizada.' });
      setTurmaEmEdicao(null);
      reload();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao atualizar turma.') });
    }
  };

  useEffect(() => {
    if (master) {
      HttpService.listarTodasInstituicoes()
        .then((r) => setInstituicoes(mapInstituicoesApi(r.data || [])))
        .catch(() => setInstituicoes([]));
    }
  }, [master]);

  useEffect(() => {
    if (!somenteCadastro) reload();
  }, [somenteCadastro, filtroInstituicao, filtroProfessor, filtroDias]);

  useEffect(() => {
    const id = instituicaoIdFiltroProf;
    if (!id || somenteCadastro) {
      setProfessoresFiltro([]);
      return;
    }
    HttpService.professoresInstituicao(id)
      .then((r) => setProfessoresFiltro(r.data))
      .catch(() => setProfessoresFiltro([]));
  }, [instituicaoIdFiltroProf, somenteCadastro]);

  useEffect(() => {
    const id = instituicaoIdAtiva;
    if (!id || !somenteCadastro) return;
    HttpService.professoresInstituicao(id)
      .then((r) => setProfessores(r.data))
      .catch(() => setProfessores([]));
    HttpService.programacaoListarSalas(id)
      .then((r) => setSalas(r.data))
      .catch(() => setSalas([]));
  }, [instituicaoIdAtiva, somenteCadastro]);

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
    if (!instituicaoIdAtiva) {
      setModal({ open: true, success: false, message: master ? 'Selecione a instituição da turma.' : 'Instituição não identificada na sessão.' });
      return;
    }
    try {
      await HttpService.montarTurma({
        instituicaoId: Number(instituicaoIdAtiva),
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
      {somenteCadastro ? (
          <div className="card turmas-form-section">
            <h3 style={{ marginTop: 0 }}>Nova turma</h3>
            <p className="field-hint">Professor opcional; pode vincular depois na lista.</p>

            {master && (
              <div className="form-grid__span-2">
                <label>Instituição</label>
                <select
                  value={instituicaoCadastro}
                  onChange={(e) => {
                    setInstituicaoCadastro(e.target.value);
                    setSala('');
                    setCpfProfessor('');
                  }}
                >
                  <option value="">Selecione a instituição</option>
                  {instituicoes.map((i) => (
                    <option key={i.id} value={String(i.id)}>{i.razaoSocial}</option>
                  ))}
                </select>
              </div>
            )}

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
      ) : (
        <div className="turmas-layout">
        <div className="card" style={{ gridColumn: '1 / -1', marginBottom: '1rem' }}>
          <h3 style={{ marginTop: 0 }}>Filtros</h3>
          <div className="form-grid">
            {master && (
              <div>
                <label>Instituição</label>
                <select
                  value={filtroInstituicao}
                  onChange={(e) => {
                    setFiltroInstituicao(e.target.value);
                    setFiltroProfessor('');
                  }}
                >
                  <option value="">Todas</option>
                  {instituicoes.map((i) => (
                    <option key={i.id} value={String(i.id)}>{i.razaoSocial}</option>
                  ))}
                </select>
              </div>
            )}
            <div>
              <label>Professor</label>
              <select
                value={filtroProfessor}
                onChange={(e) => setFiltroProfessor(e.target.value)}
                disabled={!instituicaoIdFiltroProf && master}
              >
                <option value="">Todos</option>
                {professoresFiltro.map((p) => (
                  <option key={p.cpf} value={p.cpf}>{p.nome}</option>
                ))}
              </select>
            </div>
          </div>
          <DiasSemanaMultiSelect value={filtroDias} onChange={setFiltroDias} />
          <div className="form-actions form-actions--compact">
            <button
              type="button"
              className="btn-secondary"
              onClick={() => {
                setFiltroInstituicao('');
                setFiltroProfessor('');
                setFiltroDias([]);
              }}
            >
              Limpar filtros
            </button>
          </div>
        </div>
        <div className="card turmas-list-card" style={{ gridColumn: '1 / -1' }}>
          <h3>Turmas ({turmas.length})</h3>
          {turmas.length === 0 && <p className="field-hint">Nenhuma turma cadastrada.</p>}
          {turmas.map((t) => (
            <div key={t.id} className="turmas-item">
              <strong>{t.modalidade}</strong>
              {master && t.instituicao?.razaoSocial && (
                <p className="field-hint" style={{ margin: '0.25rem 0' }}>
                  Instituição: {t.instituicao.razaoSocial}
                </p>
              )}
              <div className="turmas-item__meta">
                <span className="turmas-badge">{t.horario}</span>
                {t.sala && <span className="turmas-badge">{t.sala}</span>}
                {t.dias?.map((d) => <span key={d} className="turmas-badge">{d}</span>)}
                <span className="turmas-badge">Prof.: {t.professor?.nome || 'Não vinculado'}</span>
              </div>
              {podeGerenciar && (
                <>
                  {turmaEmEdicao === t.id ? (
                    <div className="card" style={{ marginTop: '0.75rem', padding: '1rem' }}>
                      <h4 style={{ marginTop: 0 }}>Editar turma</h4>
                      <div className="form-grid">
                        <div>
                          <label>Modalidade</label>
                          <input value={editModalidade} onChange={(e) => setEditModalidade(e.target.value)} />
                        </div>
                        <div>
                          <label>Sala</label>
                          <select value={editSala} onChange={(e) => setEditSala(e.target.value)}>
                            <option value="">Sem sala definida</option>
                            {(master ? salasPorTurma[t.id] : salas).map((s) => (
                              <option key={s.id} value={s.nome}>{s.nome}</option>
                            ))}
                          </select>
                        </div>
                      </div>
                      <div className="horario-range" style={{ marginTop: '0.5rem' }}>
                        <label>Horário da aula</label>
                        <div className="turmas-horario-row">
                          <div>
                            <span className="horario-range__legend">Início</span>
                            <input type="time" value={editHoraInicio} onChange={(e) => setEditHoraInicio(e.target.value)} />
                          </div>
                          <span className="horario-range__sep">até</span>
                          <div>
                            <span className="horario-range__legend">Término</span>
                            <input type="time" value={editHoraFim} onChange={(e) => setEditHoraFim(e.target.value)} />
                          </div>
                        </div>
                      </div>
                      <DiasSemanaMultiSelect value={editDias} onChange={setEditDias} />
                      <div className="form-actions form-actions--compact">
                        <button type="button" className="btn-primary" onClick={() => salvarEdicaoTurma(t.id)}>Salvar dados</button>
                        <button type="button" className="btn-secondary" onClick={cancelarEdicao}>Cancelar</button>
                      </div>
                    </div>
                  ) : (
                    <div className="form-actions form-actions--compact" style={{ marginTop: '0.5rem' }}>
                      <button
                        type="button"
                        className="btn-secondary"
                        onClick={() => {
                          iniciarEdicao(t);
                          if (!master && sessao?.vinculo) {
                            HttpService.programacaoListarSalas(sessao.vinculo)
                              .then((r) => setSalas(r.data))
                              .catch(() => setSalas([]));
                          }
                        }}
                      >
                        Editar dados
                      </button>
                    </div>
                  )}
                  <div className="form-grid" style={{ marginTop: '0.75rem', maxWidth: 420 }}>
                    <div>
                      <label>Vincular professor</label>
                      <select
                        value={vinculoProfTurma[t.id] ?? t.professor?.cpf ?? ''}
                        onFocus={() => master && carregarProfessoresTurma(t)}
                        onChange={(e) => setVinculoProfTurma((prev) => ({ ...prev, [t.id]: e.target.value }))}
                      >
                        <option value="">Remover professor</option>
                        {(master ? professoresPorTurma[t.id] : professores)?.map((p) => (
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

export default GerenciarTurmas;

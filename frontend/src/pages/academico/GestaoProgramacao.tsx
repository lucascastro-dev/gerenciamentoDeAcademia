import { useCallback, useEffect, useMemo, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import { carregarSessao, isModoPlataforma, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { mapInstituicoesApi } from '../../utils/instituicao';
import '../../theme/programacao.css';

type Aba = 'itens' | 'grade' | 'salas';

interface TipoProgramacao {
  codigo: string;
  descricao: string;
}

interface ItemProgramacao {
  id: number;
  escopoLancamento?: string;
  cpfAluno?: string;
  nomeAluno?: string;
  turmaId?: number;
  nomeTurma?: string;
  tipo: string;
  tipoDescricao?: string;
  titulo: string;
  descricao?: string;
  dataPrevista?: string;
  dataFim?: string;
  horario?: string;
  horaInicio?: string;
  horaFim?: string;
  sala?: string;
}

interface GradeEvento {
  origem: string;
  referenciaId?: number;
  titulo: string;
  subtitulo?: string;
  modalidade?: string;
  sala?: string;
  diaSemana?: string;
  data?: string;
  horaInicio?: string;
  horaFim?: string;
  conflito?: boolean;
}

interface Sala {
  id: number;
  nome: string;
  capacidade?: number;
  ativa?: boolean;
}

interface AlunoOpt {
  cpf: string;
  nome: string;
}

interface TurmaOpt {
  id: number;
  modalidade: string;
  horario?: string;
}

const DIAS_GRADE = [
  'segunda-feira',
  'terça-feira',
  'quarta-feira',
  'quinta-feira',
  'sexta-feira',
  'sábado',
  'domingo',
];

const DIAS_LABEL: Record<string, string> = {
  'segunda-feira': 'Seg',
  'terça-feira': 'Ter',
  'quarta-feira': 'Qua',
  'quinta-feira': 'Qui',
  'sexta-feira': 'Sex',
  'sábado': 'Sáb',
  'domingo': 'Dom',
};

function segundaDaSemana(d: Date): string {
  const copy = new Date(d);
  const day = copy.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  copy.setDate(copy.getDate() + diff);
  return copy.toISOString().slice(0, 10);
}

function formatHora(h?: string): string {
  if (!h) return '';
  return h.length >= 5 ? h.slice(0, 5) : h;
}

function parseHorario(horario?: string, horaInicio?: string, horaFim?: string) {
  if (horaInicio && horaFim) {
    return { inicio: horaInicio.slice(0, 5), fim: horaFim.slice(0, 5) };
  }
  const partes = (horario || '').split('-').map((p) => p.trim());
  return { inicio: partes[0]?.slice(0, 5) || '18:00', fim: partes[1]?.slice(0, 5) || '19:30' };
}

function validarIntervalo(horaInicio: string, horaFim: string): string | null {
  if (!horaInicio || !horaFim) return 'Informe o horário de início e de término.';
  if (horaFim <= horaInicio) return 'O horário de término deve ser depois do início.';
  return null;
}

function montarPayload(form: ReturnType<typeof formVazio>) {
  return {
    escopoLancamento: form.escopoLancamento,
    cpfAluno: form.escopoLancamento === 'ALUNO' ? form.cpfAluno : undefined,
    turmaId: form.escopoLancamento === 'TURMA' ? Number(form.turmaId) : undefined,
    tipo: form.tipo,
    titulo: form.titulo,
    descricao: form.descricao,
    dataPrevista: form.dataPrevista,
    dataFim: form.dataFim || null,
    horaInicio: form.horaInicio,
    horaFim: form.horaFim,
    sala: form.sala,
  };
}

const formVazio = () => ({
  escopoLancamento: 'ALUNO' as 'ALUNO' | 'TURMA',
  cpfAluno: '',
  turmaId: '',
  tipo: 'AULA',
  titulo: '',
  descricao: '',
  dataPrevista: '',
  dataFim: '',
  horaInicio: '18:00',
  horaFim: '19:30',
  sala: '',
});

interface InstituicaoOpt {
  id: number;
  razaoSocial: string;
}

const GestaoProgramacao: React.FC = () => {
  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);
  const vinculoPadrao = sessao?.vinculo && sessao.vinculo !== '0' ? sessao.vinculo : '';
  const [instituicaoId, setInstituicaoId] = useState(vinculoPadrao || '');
  const [instituicoes, setInstituicoes] = useState<InstituicaoOpt[]>([]);
  const podeGerenciarItens = possuiPermissao(sessao, 'programacao:gerenciar')
    || possuiPermissao(sessao, 'programacao:gerenciar-itens');
  const podeGerenciarSalas = possuiPermissao(sessao, 'programacao:gerenciar');

  const [aba, setAba] = useState<Aba>('itens');
  const [tipos, setTipos] = useState<TipoProgramacao[]>([]);
  const [itens, setItens] = useState<ItemProgramacao[]>([]);
  const [grade, setGrade] = useState<GradeEvento[]>([]);
  const [salas, setSalas] = useState<Sala[]>([]);
  const [alunos, setAlunos] = useState<AlunoOpt[]>([]);
  const [turmas, setTurmas] = useState<TurmaOpt[]>([]);
  const [semanaRef, setSemanaRef] = useState(() => segundaDaSemana(new Date()));
  const [form, setForm] = useState(formVazio());
  const [editId, setEditId] = useState<number | null>(null);
  const [conflitos, setConflitos] = useState<string[]>([]);
  const [novaSala, setNovaSala] = useState({ nome: '', capacidade: '' });
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const [carregando, setCarregando] = useState(false);

  useEffect(() => {
    if (master) {
      HttpService.listarTodasInstituicoes()
        .then((r) => setInstituicoes(mapInstituicoesApi(r.data || [])))
        .catch(() => setInstituicoes([]));
    } else if (vinculoPadrao) {
      setInstituicaoId(vinculoPadrao);
    }
  }, [master, vinculoPadrao]);

  const aoTrocarInstituicao = (novoId: string) => {
    setInstituicaoId(novoId);
    setItens([]);
    setGrade([]);
    setSalas([]);
    setAlunos([]);
    setTurmas([]);
    setTipos([]);
    setEditId(null);
    setForm(formVazio());
    setConflitos([]);
  };

  const carregarItens = useCallback(() => {
    if (!instituicaoId) return;
    HttpService.programacaoListarItens(instituicaoId)
      .then((r) => setItens(r.data))
      .catch(() => setItens([]));
  }, [instituicaoId]);

  const carregarGrade = useCallback(() => {
    if (!instituicaoId) return;
    HttpService.programacaoGrade(instituicaoId, semanaRef)
      .then((r) => setGrade(r.data))
      .catch(() => setGrade([]));
  }, [instituicaoId, semanaRef]);

  const carregarSalas = useCallback(() => {
    if (!instituicaoId) return;
    HttpService.programacaoListarSalas(instituicaoId)
      .then((r) => setSalas(r.data))
      .catch(() => setSalas([]));
  }, [instituicaoId]);

  useEffect(() => {
    if (!instituicaoId) {
      setTipos([]);
      setItens([]);
      setGrade([]);
      setSalas([]);
      setAlunos([]);
    setTurmas([]);
      return;
    }
    HttpService.programacaoTipos(instituicaoId).then((r) => setTipos(r.data)).catch(() => undefined);
    carregarSalas();
    if (podeGerenciarItens) {
      HttpService.listarAlunos(instituicaoId)
        .then((r) => setAlunos((r.data || []).map((a: AlunoOpt) => ({ cpf: a.cpf, nome: a.nome }))))
        .catch(() => setAlunos([]));
      HttpService.listarTurmas({ instituicaoId: Number(instituicaoId) })
        .then((r) => setTurmas((r.data || []).map((t: TurmaOpt) => ({
          id: t.id,
          modalidade: t.modalidade,
          horario: t.horario,
        }))))
        .catch(() => setTurmas([]));
    }
  }, [instituicaoId, podeGerenciarItens, carregarSalas]);

  useEffect(() => {
    if (!instituicaoId) return;
    if (aba === 'itens') carregarItens();
    if (aba === 'grade') carregarGrade();
    if (aba === 'salas') carregarSalas();
  }, [aba, instituicaoId, carregarItens, carregarGrade, carregarSalas]);

  const kpis = useMemo(() => {
    const conflitosGrade = grade.filter((e) => e.conflito).length;
    return {
      itens: itens.length,
      turmasGrade: grade.filter((e) => e.origem === 'TURMA').length,
      programacaoGrade: grade.filter((e) => e.origem === 'PROGRAMACAO').length,
      conflitos: conflitosGrade,
      salas: salas.length,
    };
  }, [itens, grade, salas]);

  const eventosPorDia = useMemo(() => {
    const map: Record<string, GradeEvento[]> = {};
    DIAS_GRADE.forEach((d) => { map[d] = []; });
    grade.forEach((ev) => {
      const chave = (ev.diaSemana || '').toLowerCase();
      if (map[chave]) map[chave].push(ev);
      else if (ev.data) {
        const d = new Date(`${ev.data}T12:00:00`);
        const nomes = ['domingo', 'segunda-feira', 'terça-feira', 'quarta-feira', 'quinta-feira', 'sexta-feira', 'sábado'];
        const k = nomes[d.getDay()];
        if (map[k]) map[k].push(ev);
      }
    });
    Object.values(map).forEach((lista) =>
      lista.sort((a, b) => (formatHora(a.horaInicio) > formatHora(b.horaInicio) ? 1 : -1)),
    );
    return map;
  }, [grade]);

  const limparForm = () => {
    setForm(formVazio());
    setEditId(null);
    setConflitos([]);
  };

  const preencherEdicao = (item: ItemProgramacao) => {
    const horario = parseHorario(item.horario, item.horaInicio, item.horaFim);
    setEditId(item.id);
    setForm({
      escopoLancamento: item.escopoLancamento === 'TURMA' ? 'TURMA' : 'ALUNO',
      cpfAluno: item.cpfAluno || '',
      turmaId: item.turmaId ? String(item.turmaId) : '',
      tipo: item.tipo,
      titulo: item.titulo,
      descricao: item.descricao || '',
      dataPrevista: item.dataPrevista || '',
      dataFim: item.dataFim || '',
      horaInicio: horario.inicio,
      horaFim: horario.fim,
      sala: item.sala || '',
    });
    setConflitos([]);
  };

  const validarConflitos = async (): Promise<string[]> => {
    const erroHorario = validarIntervalo(form.horaInicio, form.horaFim);
    if (erroHorario) {
      setConflitos([erroHorario]);
      return [erroHorario];
    }
    if (!form.sala?.trim() || !form.dataPrevista) {
      setConflitos([]);
      return [];
    }
    try {
      const payload = montarPayload(form);
      const r = await HttpService.programacaoValidarConflito(instituicaoId, payload, editId ?? undefined);
      const msgs = (r.data || []).map((c: { mensagem: string }) => c.mensagem);
      setConflitos(msgs);
      return msgs;
    } catch (e) {
      const msg = extractApiMessage(e, 'Erro ao validar conflitos.');
      setConflitos([msg]);
      return [msg];
    }
  };

  const salvarItem = async () => {
    if (!podeGerenciarItens) return;
    const erroHorario = validarIntervalo(form.horaInicio, form.horaFim);
    if (erroHorario) {
      setModal({ open: true, success: false, message: erroHorario });
      return;
    }
    if (form.escopoLancamento === 'ALUNO' && !form.cpfAluno) {
      setModal({ open: true, success: false, message: 'Selecione o aluno.' });
      return;
    }
    if (form.escopoLancamento === 'TURMA' && !form.turmaId) {
      setModal({ open: true, success: false, message: 'Selecione a turma.' });
      return;
    }
    if (!form.sala) {
      setModal({ open: true, success: false, message: 'Selecione uma sala cadastrada.' });
      return;
    }
    setCarregando(true);
    try {
      const msgsConflito = await validarConflitos();
      if (msgsConflito.length > 0) {
        setModal({
          open: true,
          success: false,
          message: 'Não é possível publicar: resolva os conflitos de horário/sala antes de salvar.',
        });
        setCarregando(false);
        return;
      }
      const payload = montarPayload(form);
      if (editId) {
        await HttpService.programacaoAtualizarItem(instituicaoId, editId, payload);
      } else {
        await HttpService.programacaoCriarItem(instituicaoId, payload);
      }
      setModal({ open: true, success: true, message: editId ? 'Item atualizado.' : 'Item publicado na programação.' });
      limparForm();
      carregarItens();
      if (aba === 'grade') carregarGrade();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao salvar item.') });
    } finally {
      setCarregando(false);
    }
  };

  const excluirItem = async (id: number) => {
    if (!window.confirm('Excluir este item da programação?')) return;
    try {
      await HttpService.programacaoExcluirItem(instituicaoId, id);
      setModal({ open: true, success: true, message: 'Item removido.' });
      carregarItens();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  const criarSala = async () => {
    if (!novaSala.nome.trim()) return;
    try {
      await HttpService.programacaoCriarSala(instituicaoId, {
        nome: novaSala.nome.trim(),
        capacidade: novaSala.capacidade ? Number(novaSala.capacidade) : null,
        ativa: true,
      });
      setNovaSala({ nome: '', capacidade: '' });
      carregarSalas();
      setModal({ open: true, success: true, message: 'Sala cadastrada.' });
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  const excluirSala = async (id: number) => {
    if (!window.confirm('Excluir esta sala?')) return;
    try {
      await HttpService.programacaoExcluirSala(instituicaoId, id);
      carregarSalas();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  const mudarSemana = (delta: number) => {
    const d = new Date(`${semanaRef}T12:00:00`);
    d.setDate(d.getDate() + delta * 7);
    setSemanaRef(segundaDaSemana(d));
  };

  return (
    <PageShell>
      {master && (
        <div className="card" style={{ marginBottom: '1rem' }}>
          <label>Instituição</label>
          <select
            value={instituicaoId}
            onChange={(e) => aoTrocarInstituicao(e.target.value)}
            style={{ marginTop: '0.35rem', maxWidth: 420 }}
          >
            <option value="">Selecione a instituição para gerenciar a programação</option>
            {instituicoes.map((i) => (
              <option key={i.id} value={String(i.id)}>{i.razaoSocial}</option>
            ))}
          </select>
        </div>
      )}

      {!instituicaoId && master && (
        <p className="field-hint">Escolha uma instituição para carregar itens, grade e salas.</p>
      )}

      {instituicaoId && (
      <>
      <div className="programacao-kpis">
        <div className="programacao-kpi"><strong>{kpis.itens}</strong><span>Itens programados</span></div>
        <div className="programacao-kpi"><strong>{kpis.turmasGrade + kpis.programacaoGrade}</strong><span>Eventos na semana</span></div>
        <div className="programacao-kpi"><strong>{kpis.conflitos}</strong><span>Conflitos detectados</span></div>
        <div className="programacao-kpi"><strong>{kpis.salas}</strong><span>Salas ativas</span></div>
      </div>

      <div className="programacao-tabs">
        <button type="button" className={aba === 'itens' ? 'active' : ''} onClick={() => setAba('itens')}>
          Itens (Minha programação)
        </button>
        <button type="button" className={aba === 'grade' ? 'active' : ''} onClick={() => setAba('grade')}>
          Grade horária
        </button>
        <button type="button" className={aba === 'salas' ? 'active' : ''} onClick={() => setAba('salas')}>
          Salas
        </button>
      </div>

      {aba === 'itens' && (
        <>
          {podeGerenciarItens && (
            <div className="programacao-form-panel">
              <h3 style={{ marginTop: 0 }}>{editId ? 'Editar item' : 'Novo lançamento'}</h3>
              <div className="form-grid">
                <div>
                  <label>Tipo de lançamento</label>
                  <select
                    value={form.escopoLancamento}
                    onChange={(e) => setForm((f) => ({
                      ...f,
                      escopoLancamento: e.target.value as 'ALUNO' | 'TURMA',
                      cpfAluno: '',
                      turmaId: '',
                    }))}
                  >
                    <option value="ALUNO">Aluno específico</option>
                    <option value="TURMA">Turma completa</option>
                  </select>
                </div>
                {form.escopoLancamento === 'ALUNO' ? (
                  <div>
                    <label>Aluno</label>
                    <select
                      value={form.cpfAluno}
                      onChange={(e) => setForm((f) => ({ ...f, cpfAluno: e.target.value }))}
                    >
                      <option value="">Selecione</option>
                      {alunos.map((a) => (
                        <option key={a.cpf} value={a.cpf}>{a.nome}</option>
                      ))}
                    </select>
                  </div>
                ) : (
                  <div>
                    <label>Turma</label>
                    <select
                      value={form.turmaId}
                      onChange={(e) => setForm((f) => ({ ...f, turmaId: e.target.value }))}
                    >
                      <option value="">Selecione</option>
                      {turmas.map((t) => (
                        <option key={t.id} value={String(t.id)}>{t.modalidade}{t.horario ? ` · ${t.horario}` : ''}</option>
                      ))}
                    </select>
                  </div>
                )}
                <div>
                  <label>Tipo</label>
                  <select value={form.tipo} onChange={(e) => setForm((f) => ({ ...f, tipo: e.target.value }))}>
                    {tipos.map((t) => (
                      <option key={t.codigo} value={t.codigo}>{t.descricao}</option>
                    ))}
                  </select>
                </div>
                <div><label>Título</label><input value={form.titulo} onChange={(e) => setForm((f) => ({ ...f, titulo: e.target.value }))} /></div>
                <div><label>Data início</label><input type="date" value={form.dataPrevista} onChange={(e) => setForm((f) => ({ ...f, dataPrevista: e.target.value }))} /></div>
                <div><label>Data fim (opcional)</label><input type="date" value={form.dataFim} onChange={(e) => setForm((f) => ({ ...f, dataFim: e.target.value }))} /></div>
                <div><label>Início</label><input type="time" value={form.horaInicio} onChange={(e) => setForm((f) => ({ ...f, horaInicio: e.target.value }))} /></div>
                <div><label>Término</label><input type="time" value={form.horaFim} onChange={(e) => setForm((f) => ({ ...f, horaFim: e.target.value }))} /></div>
                <div>
                  <label>Sala</label>
                  <select value={form.sala} onChange={(e) => setForm((f) => ({ ...f, sala: e.target.value }))}>
                    <option value="">Selecione</option>
                    {salas.filter((s) => s.ativa !== false).map((s) => (
                      <option key={s.id} value={s.nome}>{s.nome}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <label>Descrição</label>
                <textarea rows={2} value={form.descricao} onChange={(e) => setForm((f) => ({ ...f, descricao: e.target.value }))} style={{ width: '100%' }} />
              </div>
              {conflitos.length > 0 && (
                <div className="card" style={{ marginTop: '0.75rem', borderColor: '#fecaca', background: '#fef2f2' }}>
                  <strong>Conflitos de horário</strong>
                  <ul style={{ margin: '0.5rem 0 0', paddingLeft: '1.2rem' }}>
                    {conflitos.map((c) => <li key={c}>{c}</li>)}
                  </ul>
                </div>
              )}
              <div className="form-actions form-actions--compact" style={{ marginTop: '1rem' }}>
                <button type="button" className="btn-secondary" onClick={() => validarConflitos()}>Verificar conflito</button>
                <button
                  type="button"
                  className="btn-primary"
                  disabled={carregando || conflitos.length > 0}
                  onClick={salvarItem}
                >
                  {editId ? 'Salvar alterações' : 'Publicar'}
                </button>
                {editId && <button type="button" className="btn-secondary" onClick={limparForm}>Cancelar edição</button>}
              </div>
            </div>
          )}

          <div className="card">
            <table className="programacao-table">
              <thead>
                <tr>
                  <th>Destino</th>
                  <th>Tipo</th>
                  <th>Título</th>
                  <th>Quando</th>
                  <th>Sala</th>
                  {podeGerenciarItens && <th />}
                </tr>
              </thead>
              <tbody>
                {itens.length === 0 && (
                  <tr><td colSpan={podeGerenciarItens ? 6 : 5}>Nenhum item cadastrado.</td></tr>
                )}
                {itens.map((item) => (
                  <tr key={item.id}>
                    <td>
                      {item.escopoLancamento === 'TURMA'
                        ? `Turma: ${item.nomeTurma || item.turmaId}`
                        : (item.nomeAluno || item.cpfAluno)}
                    </td>
                    <td><span className={`tipo-badge tipo-badge--${item.tipo}`}>{item.tipoDescricao || item.tipo}</span></td>
                    <td>{item.titulo}</td>
                    <td>
                      {item.dataPrevista}
                      {item.dataFim && item.dataFim !== item.dataPrevista ? ` – ${item.dataFim}` : ''}
                      {(item.horario || (item.horaInicio && item.horaFim))
                        ? ` · ${item.horario || `${formatHora(item.horaInicio)}–${formatHora(item.horaFim)}`}`
                        : ''}
                    </td>
                    <td>{item.sala || '—'}</td>
                    {podeGerenciarItens && (
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <button type="button" className="btn-secondary" style={{ marginRight: 6 }} onClick={() => preencherEdicao(item)}>Editar</button>
                        <button type="button" className="btn-secondary" onClick={() => excluirItem(item.id)}>Excluir</button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {aba === 'grade' && (
        <div className="card">
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', alignItems: 'center', marginBottom: '1rem' }}>
            <button type="button" className="btn-secondary" onClick={() => mudarSemana(-1)}>← Semana anterior</button>
            <span>Semana de <strong>{semanaRef}</strong></span>
            <button type="button" className="btn-secondary" onClick={() => mudarSemana(1)}>Próxima semana →</button>
            <input type="date" value={semanaRef} onChange={(e) => setSemanaRef(segundaDaSemana(new Date(`${e.target.value}T12:00:00`)))} />
          </div>
          <p className="field-hint" style={{ marginTop: 0 }}>
            Turmas recorrentes e itens da programação na mesma visão. Eventos em vermelho indicam sobreposição na mesma sala.
          </p>
          <div className="grade-grid">
            <div className="grade-grid__head">Horário</div>
            {DIAS_GRADE.map((d) => (
              <div key={d} className="grade-grid__head">{DIAS_LABEL[d]}</div>
            ))}
            <div className="grade-grid__time">Dia todo</div>
            {DIAS_GRADE.map((d) => (
              <div key={d} className="grade-grid__cell">
                {eventosPorDia[d]?.length === 0 && <span className="field-hint">—</span>}
                {eventosPorDia[d]?.map((ev, idx) => (
                  <div
                    key={`${ev.origem}-${ev.referenciaId}-${idx}`}
                    className={`grade-event ${ev.origem === 'TURMA' ? 'grade-event--turma' : ''} ${ev.conflito ? 'grade-event--conflito' : ''}`}
                  >
                    <strong>{ev.titulo}</strong>
                    {ev.subtitulo && <span> · {ev.subtitulo}</span>}
                    <br />
                    {formatHora(ev.horaInicio)}{ev.horaFim ? `–${formatHora(ev.horaFim)}` : ''}
                    {ev.sala && <> · {ev.sala}</>}
                    {ev.conflito && <> · conflito</>}
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>
      )}

      {aba === 'salas' && (
        <>
          {podeGerenciarSalas && (
            <div className="programacao-form-panel">
              <h3 style={{ marginTop: 0 }}>Nova sala</h3>
              <div className="form-grid">
                <div><label>Nome</label><input value={novaSala.nome} onChange={(e) => setNovaSala((s) => ({ ...s, nome: e.target.value }))} placeholder="Dojo 1" /></div>
                <div><label>Capacidade</label><input type="number" min={1} value={novaSala.capacidade} onChange={(e) => setNovaSala((s) => ({ ...s, capacidade: e.target.value }))} /></div>
              </div>
              <div className="form-actions form-actions--compact">
                <button type="button" className="btn-primary" onClick={criarSala}>Cadastrar sala</button>
              </div>
            </div>
          )}
          <div className="card">
            <table className="programacao-table">
              <thead><tr><th>Sala</th><th>Capacidade</th><th>Status</th>{podeGerenciarSalas ? <th>Ações</th> : null}</tr></thead>
              <tbody>
                {salas.map((s) => (
                  <tr key={s.id}>
                    <td>{s.nome}</td>
                    <td>{s.capacidade ?? '—'}</td>
                    <td>{s.ativa !== false ? 'Ativa' : 'Inativa'}</td>
                    {podeGerenciarSalas && (
                      <td><button type="button" className="btn-secondary" onClick={() => excluirSala(s.id)}>Excluir</button></td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
      </>
      )}

      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default GestaoProgramacao;

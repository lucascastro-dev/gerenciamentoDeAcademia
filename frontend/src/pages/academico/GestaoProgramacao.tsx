import { useCallback, useEffect, useMemo, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import { carregarSessao, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import '../../theme/programacao.css';

type Aba = 'itens' | 'grade' | 'salas';

interface TipoProgramacao {
  codigo: string;
  descricao: string;
}

interface ItemProgramacao {
  id: number;
  cpfAluno?: string;
  nomeAluno?: string;
  tipo: string;
  tipoDescricao?: string;
  titulo: string;
  descricao?: string;
  dataPrevista?: string;
  horario?: string;
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

const formVazio = () => ({
  cpfAluno: '',
  tipo: 'AULA',
  titulo: '',
  descricao: '',
  dataPrevista: '',
  horario: '',
  sala: '',
});

const GestaoProgramacao: React.FC = () => {
  const sessao = carregarSessao();
  const instituicaoId = sessao?.vinculo || '1';
  const podeGerenciar = possuiPermissao(sessao, 'programacao:gerenciar');

  const [aba, setAba] = useState<Aba>('itens');
  const [tipos, setTipos] = useState<TipoProgramacao[]>([]);
  const [itens, setItens] = useState<ItemProgramacao[]>([]);
  const [grade, setGrade] = useState<GradeEvento[]>([]);
  const [salas, setSalas] = useState<Sala[]>([]);
  const [alunos, setAlunos] = useState<AlunoOpt[]>([]);
  const [semanaRef, setSemanaRef] = useState(() => segundaDaSemana(new Date()));
  const [form, setForm] = useState(formVazio());
  const [editId, setEditId] = useState<number | null>(null);
  const [conflitos, setConflitos] = useState<string[]>([]);
  const [novaSala, setNovaSala] = useState({ nome: '', capacidade: '' });
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const [carregando, setCarregando] = useState(false);

  const carregarItens = useCallback(() => {
    HttpService.programacaoListarItens(instituicaoId)
      .then((r) => setItens(r.data))
      .catch(() => setItens([]));
  }, [instituicaoId]);

  const carregarGrade = useCallback(() => {
    HttpService.programacaoGrade(instituicaoId, semanaRef)
      .then((r) => setGrade(r.data))
      .catch(() => setGrade([]));
  }, [instituicaoId, semanaRef]);

  const carregarSalas = useCallback(() => {
    HttpService.programacaoListarSalas(instituicaoId)
      .then((r) => setSalas(r.data))
      .catch(() => setSalas([]));
  }, [instituicaoId]);

  useEffect(() => {
    HttpService.programacaoTipos(instituicaoId).then((r) => setTipos(r.data)).catch(() => undefined);
    carregarSalas();
    if (podeGerenciar) {
      HttpService.listarAlunos()
        .then((r) => setAlunos((r.data || []).map((a: AlunoOpt) => ({ cpf: a.cpf, nome: a.nome }))))
        .catch(() => setAlunos([]));
    }
  }, [instituicaoId, podeGerenciar, carregarSalas]);

  useEffect(() => {
    if (aba === 'itens') carregarItens();
    if (aba === 'grade') carregarGrade();
    if (aba === 'salas') carregarSalas();
  }, [aba, carregarItens, carregarGrade, carregarSalas]);

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
    setEditId(item.id);
    setForm({
      cpfAluno: item.cpfAluno || '',
      tipo: item.tipo,
      titulo: item.titulo,
      descricao: item.descricao || '',
      dataPrevista: item.dataPrevista || '',
      horario: item.horario || '',
      sala: item.sala || '',
    });
    setConflitos([]);
  };

  const validarConflitos = async (): Promise<string[]> => {
    if (!form.sala?.trim() || !form.horario?.trim() || !form.dataPrevista) {
      setConflitos([]);
      return [];
    }
    try {
      const r = await HttpService.programacaoValidarConflito(instituicaoId, form, editId ?? undefined);
      const msgs = (r.data || []).map((c: { mensagem: string }) => c.mensagem);
      setConflitos(msgs);
      return msgs;
    } catch {
      setConflitos([]);
      return [];
    }
  };

  const salvarItem = async () => {
    if (!podeGerenciar) return;
    setCarregando(true);
    try {
      const msgsConflito = await validarConflitos();
      if (msgsConflito.length > 0) {
        setModal({
          open: true,
          success: false,
          message: 'Há conflito de horário/sala. Ajuste antes de salvar ou confirme se deseja prosseguir mesmo assim.',
        });
        setCarregando(false);
        return;
      }
      if (editId) {
        await HttpService.programacaoAtualizarItem(instituicaoId, editId, form);
      } else {
        await HttpService.programacaoCriarItem(instituicaoId, form);
      }
      setModal({ open: true, success: true, message: editId ? 'Item atualizado.' : 'Item criado na programação do aluno.' });
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
    <PageShell
      title="Programação e grade"
      subtitle="Organize a Minha programação dos alunos, visualize turmas na grade e evite conflitos de sala"
    >
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
          {podeGerenciar && (
            <div className="programacao-form-panel">
              <h3 style={{ marginTop: 0 }}>{editId ? 'Editar item' : 'Novo item para o aluno'}</h3>
              <p className="field-hint">
                O aluno vê estes lançamentos em Minha programação. Use horário no formato 18:00-19:30 e informe a sala para detectar conflitos.
              </p>
              <div className="form-grid">
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
                <div>
                  <label>Tipo</label>
                  <select value={form.tipo} onChange={(e) => setForm((f) => ({ ...f, tipo: e.target.value }))}>
                    {tipos.map((t) => (
                      <option key={t.codigo} value={t.codigo}>{t.descricao}</option>
                    ))}
                  </select>
                </div>
                <div><label>Título</label><input value={form.titulo} onChange={(e) => setForm((f) => ({ ...f, titulo: e.target.value }))} /></div>
                <div><label>Data</label><input type="date" value={form.dataPrevista} onChange={(e) => setForm((f) => ({ ...f, dataPrevista: e.target.value }))} /></div>
                <div><label>Horário</label><input placeholder="18:00-19:30" value={form.horario} onChange={(e) => setForm((f) => ({ ...f, horario: e.target.value }))} /></div>
                <div>
                  <label>Sala</label>
                  <input list="salas-datalist" value={form.sala} onChange={(e) => setForm((f) => ({ ...f, sala: e.target.value }))} />
                  <datalist id="salas-datalist">
                    {salas.map((s) => <option key={s.id} value={s.nome} />)}
                  </datalist>
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
                <button type="button" className="btn-primary" disabled={carregando} onClick={salvarItem}>
                  {editId ? 'Salvar alterações' : 'Publicar para o aluno'}
                </button>
                {editId && <button type="button" className="btn-secondary" onClick={limparForm}>Cancelar edição</button>}
              </div>
            </div>
          )}

          <div className="card">
            <table className="programacao-table">
              <thead>
                <tr>
                  <th>Aluno</th>
                  <th>Tipo</th>
                  <th>Título</th>
                  <th>Quando</th>
                  <th>Sala</th>
                  {podeGerenciar && <th />}
                </tr>
              </thead>
              <tbody>
                {itens.length === 0 && (
                  <tr><td colSpan={podeGerenciar ? 6 : 5}>Nenhum item cadastrado.</td></tr>
                )}
                {itens.map((item) => (
                  <tr key={item.id}>
                    <td>{item.nomeAluno || item.cpfAluno}</td>
                    <td><span className={`tipo-badge tipo-badge--${item.tipo}`}>{item.tipoDescricao || item.tipo}</span></td>
                    <td>{item.titulo}</td>
                    <td>{item.dataPrevista}{item.horario ? ` · ${item.horario}` : ''}</td>
                    <td>{item.sala || '—'}</td>
                    {podeGerenciar && (
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
          {podeGerenciar && (
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
              <thead><tr><th>Sala</th><th>Capacidade</th><th>Status</th>{podeGerenciar ? <th>Ações</th> : null}</tr></thead>
              <tbody>
                {salas.map((s) => (
                  <tr key={s.id}>
                    <td>{s.nome}</td>
                    <td>{s.capacidade ?? '—'}</td>
                    <td>{s.ativa !== false ? 'Ativa' : 'Inativa'}</td>
                    {podeGerenciar && (
                      <td><button type="button" className="btn-secondary" onClick={() => excluirSala(s.id)}>Excluir</button></td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default GestaoProgramacao;

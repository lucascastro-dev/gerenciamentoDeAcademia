import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import './Colaborador.css';

const STATUS_FILTRO = ['Todos', 'PENDENTE', 'APROVADO', 'REJEITADO', 'CANCELADO'] as const;

function formatarData(iso: string): string {
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
}

function percentual(usado: number, total: number): number {
  if (total <= 0) return 0;
  return Math.min(100, Math.round((usado / total) * 100));
}

const MinhasFerias: React.FC = () => {
  const [filtroStatus, setFiltroStatus] = useState<(typeof STATUS_FILTRO)[number]>('Todos');
  const [periodo, setPeriodo] = useState('todos');
  const [modalAberto, setModalAberto] = useState(false);
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [resumo, setResumo] = useState<Awaited<ReturnType<typeof HttpService.feriasResumo>>['data'] | null>(null);
  const [feedback, setFeedback] = useState({ open: false, success: false, message: '' });

  const carregar = useCallback(async () => {
    setCarregando(true);
    try {
      const r = await HttpService.feriasResumo();
      setResumo(r.data);
    } catch (e) {
      setFeedback({
        open: true,
        success: false,
        message: extractApiMessage(e, 'Não foi possível carregar suas férias.'),
      });
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const solicitacoesFiltradas = useMemo(() => {
    if (!resumo?.solicitacoes) return [];
    return resumo.solicitacoes.filter((s) => {
      if (filtroStatus !== 'Todos' && s.status !== filtroStatus) return false;
      if (periodo === 'atual' && resumo.periodos?.[0]) {
        const p = resumo.periodos[0];
        return s.dataInicio >= p.inicio && s.dataInicio <= p.fim;
      }
      return true;
    });
  }, [resumo, filtroStatus, periodo]);

  const diasUsados = resumo?.diasAprovadosTotal ?? 0;
  const diasDireito = 30;
  const pctSaldo = percentual(diasUsados, diasDireito);

  const abrirSolicitacao = () => {
    setDataInicio('');
    setDataFim('');
    setModalAberto(true);
  };

  const enviarSolicitacao = async (e: FormEvent) => {
    e.preventDefault();
    if (!dataInicio || !dataFim) {
      setFeedback({ open: true, success: false, message: 'Informe o período desejado.' });
      return;
    }
    if (dataFim < dataInicio) {
      setFeedback({ open: true, success: false, message: 'A data fim deve ser igual ou posterior à data início.' });
      return;
    }
    try {
      await HttpService.feriasSolicitar({ dataInicio, dataFim });
      setModalAberto(false);
      setFeedback({
        open: true,
        success: true,
        message: 'Solicitação registrada. O RH analisará seu pedido em breve.',
      });
      carregar();
    } catch (err) {
      setFeedback({
        open: true,
        success: false,
        message: extractApiMessage(err, 'Não foi possível registrar a solicitação.'),
      });
    }
  };

  const cancelar = async (id: number) => {
    try {
      await HttpService.feriasCancelar(id);
      setFeedback({ open: true, success: true, message: 'Solicitação cancelada.' });
      carregar();
    } catch (err) {
      setFeedback({
        open: true,
        success: false,
        message: extractApiMessage(err, 'Não foi possível cancelar.'),
      });
    }
  };

  return (
    <PageShell showBack={false}>
      <div className="colab-page colab-ferias">
        <div className="card colab-filter colab-filter--row">
          <div>
            <label htmlFor="filtro-status">Status</label>
            <select
              id="filtro-status"
              value={filtroStatus}
              onChange={(e) => setFiltroStatus(e.target.value as (typeof STATUS_FILTRO)[number])}
            >
              {STATUS_FILTRO.map((s) => (
                <option key={s} value={s}>{s === 'Todos' ? 'Todos' : s.charAt(0) + s.slice(1).toLowerCase()}</option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="filtro-periodo">Período aquisitivo</label>
            <select id="filtro-periodo" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
              <option value="todos">Todos</option>
              <option value="atual">Atual</option>
            </select>
          </div>
          <div className="colab-filter__action">
            <button type="button" className="btn-primary" onClick={abrirSolicitacao}>
              Solicitar férias
            </button>
          </div>
        </div>

        {carregando && <p className="field-hint">Carregando...</p>}

        {!carregando && resumo && (
          <>
            <div className="colab-ferias-grid">
              <div className="card colab-saldo">
                <h3>Saldo</h3>
                <div className="colab-saldo__row">
                  <div className="colab-saldo__circulo">{pctSaldo}%</div>
                  <div>
                    <p className="colab-saldo__dias">
                      <strong>{resumo.diasDisponiveisTotal}</strong> dias disponíveis
                    </p>
                    <p className="field-hint">
                      {resumo.diasAprovadosTotal} aprovados · {resumo.diasPendentesTotal} pendentes
                    </p>
                  </div>
                </div>
                <div className="colab-saldo__bar">
                  <span>Utilizado</span>
                  <div className="colab-saldo__track">
                    <div style={{ width: `${pctSaldo}%` }} />
                  </div>
                  <span>{pctSaldo}%</span>
                </div>
                <p className="field-hint">Direito de 30 dias a cada 365 dias de vínculo na instituição.</p>
              </div>

              <div className="card">
                <h3>Períodos aquisitivos</h3>
                <ul className="colab-periodos">
                  {resumo.periodos.map((p) => (
                    <li key={p.inicio}>
                      <span className={`colab-badge${p.situacao === 'Em aquisição' ? ' colab-badge--info' : ''}`}>
                        {p.situacao}
                      </span>
                      <span>{formatarData(p.inicio)} – {formatarData(p.fim)}</span>
                      <strong>{p.diasDisponiveis}/{p.diasDireito}</strong>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            {solicitacoesFiltradas.length === 0 ? (
              <div className="card colab-empty">
                <h3>Nenhuma solicitação</h3>
                <p className="field-hint">Use o botão acima para registrar seu primeiro pedido de férias.</p>
              </div>
            ) : (
              <div className="card table-wrap">
                <table className="audit-table">
                  <thead>
                    <tr>
                      <th>Período</th>
                      <th>Dias</th>
                      <th>Status</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {solicitacoesFiltradas.map((s) => (
                      <tr key={s.id}>
                        <td>{formatarData(s.dataInicio)} – {formatarData(s.dataFim)}</td>
                        <td>{s.diasSolicitados}</td>
                        <td>{s.statusDescricao}</td>
                        <td>
                          {s.status === 'PENDENTE' && (
                            <button type="button" className="btn-secondary" onClick={() => cancelar(s.id)}>
                              Cancelar
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>

      {modalAberto && (
        <div className="colab-modal-backdrop" role="presentation" onClick={() => setModalAberto(false)}>
          <div
            className="card colab-modal"
            role="dialog"
            aria-labelledby="modal-ferias-titulo"
            onClick={(ev) => ev.stopPropagation()}
          >
            <h3 id="modal-ferias-titulo">Solicitar férias</h3>
            <form onSubmit={enviarSolicitacao} className="colab-modal__form">
              <div className="form-grid">
                <div>
                  <label htmlFor="ferias-inicio">Data início</label>
                  <input
                    id="ferias-inicio"
                    type="date"
                    value={dataInicio}
                    onChange={(e) => setDataInicio(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label htmlFor="ferias-fim">Data fim</label>
                  <input
                    id="ferias-fim"
                    type="date"
                    value={dataFim}
                    onChange={(e) => setDataFim(e.target.value)}
                    required
                  />
                </div>
              </div>
              <div className="form-actions form-actions--compact">
                <button type="button" className="btn-secondary" onClick={() => setModalAberto(false)}>
                  Cancelar
                </button>
                <button type="submit" className="btn-primary">Enviar solicitação</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <FeedbackModal
        open={feedback.open}
        success={feedback.success}
        message={feedback.message}
        onClose={() => setFeedback((f) => ({ ...f, open: false }))}
      />
    </PageShell>
  );
};

export default MinhasFerias;

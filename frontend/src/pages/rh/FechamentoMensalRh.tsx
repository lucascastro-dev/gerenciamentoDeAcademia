import { useCallback, useEffect, useState } from 'react';
import { carregarSessao, possuiPermissao } from '../../auth/permissoes';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import '../../components/Financeiro/FinanceiroOperacional.css';
import './Rh.css';

const FechamentoMensalRh: React.FC = () => {
  const sessao = carregarSessao();
  const podeConferir = possuiPermissao(sessao, 'rh:fechamento-mensal');
  const hoje = new Date();
  const [mes, setMes] = useState(String(hoje.getMonth() + 1));
  const [ano, setAno] = useState(String(hoje.getFullYear()));
  const [integracao, setIntegracao] = useState<{
    pontoConferidoRh: boolean;
    integradoFinanceiro?: boolean;
    colaboradoresComRegistro: number;
    conferidoEm?: string;
  } | null>(null);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregar = useCallback(() => {
    HttpService.rhFolhaPontoStatusIntegracao(Number(mes), Number(ano))
      .then((r) => setIntegracao(r.data))
      .catch(() => setIntegracao(null));
  }, [mes, ano]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const conferirPonto = async () => {
    try {
      const r = await HttpService.rhFolhaPontoConferir(Number(mes), Number(ano));
      setModal({ open: true, success: true, message: r.data.message });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao conferir ponto.') });
    }
  };

  const reabrirPonto = async () => {
    if (!window.confirm('Reabrir a conferência desta competência?')) return;
    try {
      const r = await HttpService.rhFolhaPontoReabrir(Number(mes), Number(ano));
      setModal({ open: true, success: true, message: r.data.message });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao reabrir conferência.') });
    }
  };

  return (
    <PageShell showBack={false}>
      <div className="rh-page">
        <div className="card">
          <h2 style={{ marginTop: 0 }}>Conferência mensal de ponto</h2>
          <p className="field-hint">
            Conferir somente meses já encerrados. Após conferir, os colaboradores registram ponto no mês seguinte.
            Revise entradas e saídas em Folha de ponto antes de confirmar.
          </p>
        </div>

        <div className="fin-op__toolbar card">
          <div>
            <label>Competência a conferir</label>
            <div className="fin-op__competencia">
              <select value={mes} onChange={(e) => setMes(e.target.value)}>
                {Array.from({ length: 12 }, (_, i) => (
                  <option key={i + 1} value={String(i + 1)}>{String(i + 1).padStart(2, '0')}</option>
                ))}
              </select>
              <select value={ano} onChange={(e) => setAno(e.target.value)}>
                {[hoje.getFullYear(), hoje.getFullYear() - 1].map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="rh-ponto__status">
            {integracao?.pontoConferidoRh ? (
              <span className="fin-op__badge fin-op__badge--pago">Conferido</span>
            ) : (
              <span className="fin-op__badge">Pendente</span>
            )}
          </div>
        </div>

        <div className="card">
          <p>
            Colaboradores com registro no período: <strong>{integracao?.colaboradoresComRegistro ?? 0}</strong>
          </p>
          {integracao?.conferidoEm && (
            <p className="field-hint">Conferido em {new Date(integracao.conferidoEm).toLocaleString('pt-BR')}</p>
          )}
        </div>

        {podeConferir && !integracao?.pontoConferidoRh && (
          <div className="fin-op__actions">
            <button type="button" className="btn-primary" onClick={conferirPonto}>
              Conferir folha de ponto de {mes}/{ano}
            </button>
          </div>
        )}

        {podeConferir && integracao?.pontoConferidoRh && !integracao?.integradoFinanceiro && (
          <div className="fin-op__actions">
            <button type="button" className="btn-secondary" onClick={reabrirPonto}>
              Reabrir conferência de {mes}/{ano}
            </button>
          </div>
        )}
      </div>
      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default FechamentoMensalRh;

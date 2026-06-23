import { useCallback, useEffect, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import '../../components/Financeiro/FinanceiroOperacional.css';
import './Rh.css';

const ETAPAS = [
  { id: 'ponto', titulo: 'Folha de ponto conferida', descricao: 'Horas e ausências validadas pelo RH' },
  { id: 'ferias', titulo: 'Férias e afastamentos', descricao: 'Solicitações e saldos revisados' },
  { id: 'holerite', titulo: 'Holerites lançados', descricao: 'Contracheques publicados para o financeiro' },
  { id: 'integracao', titulo: 'Integração financeiro', descricao: 'Base enviada para pagamento' },
];

const FechamentoMensalRh: React.FC = () => {
  const hoje = new Date();
  const [mes, setMes] = useState(String(hoje.getMonth() + 1));
  const [ano, setAno] = useState(String(hoje.getFullYear()));
  const [etapas, setEtapas] = useState(ETAPAS.map((e) => ({ ...e, ok: false })));
  const [integracao, setIntegracao] = useState<{ pontoConferidoRh: boolean; integradoFinanceiro: boolean } | null>(null);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregarIntegracao = useCallback(() => {
    HttpService.rhFolhaPontoStatusIntegracao(Number(mes), Number(ano))
      .then((r) => {
        setIntegracao(r.data);
        setEtapas((lista) => lista.map((e) => {
          if (e.id === 'ponto') return { ...e, ok: r.data.pontoConferidoRh };
          if (e.id === 'integracao') return { ...e, ok: r.data.integradoFinanceiro };
          return e;
        }));
      })
      .catch(() => setIntegracao(null));
  }, [mes, ano]);

  useEffect(() => {
    carregarIntegracao();
  }, [carregarIntegracao]);

  const progresso = Math.round((etapas.filter((e) => e.ok).length / etapas.length) * 100);

  const fechar = () => {
    if (progresso < 100) {
      setModal({ open: true, success: false, message: 'Conclua todas as etapas do RH antes de fechar.' });
      return;
    }
    if (!integracao?.pontoConferidoRh) {
      setModal({ open: true, success: false, message: 'Conferir a folha de ponto em Recursos Humanos → Folha de ponto.' });
      return;
    }
    setModal({ open: true, success: true, message: `Fechamento de RH de ${mes}/${ano} registrado.` });
  };

  const conferirPonto = async () => {
    try {
      const r = await HttpService.rhFolhaPontoConferir(Number(mes), Number(ano));
      setModal({ open: true, success: true, message: r.data.message });
      carregarIntegracao();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao conferir ponto.') });
    }
  };

  return (
    <PageShell showBack={false}>
      <div className="rh-page">
        <div className="fin-op__toolbar card">
          <div>
            <label>Competência</label>
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
          <div className="fin-op__progresso">
            <span>{progresso}% concluído</span>
            <div className="fin-op__progresso-track"><div style={{ width: `${progresso}%` }} /></div>
          </div>
        </div>

        {!integracao?.pontoConferidoRh && (
          <div className="fin-op__actions">
            <button type="button" className="btn-secondary" onClick={conferirPonto}>
              Conferir folha de ponto agora
            </button>
          </div>
        )}

        <div className="fin-op__etapas">
          {etapas.map((e) => (
            <button
              key={e.id}
              type="button"
              className={`fin-op__etapa card${e.ok ? ' fin-op__etapa--ok' : ''}`}
              onClick={() => {
                if (e.id === 'ponto' || e.id === 'integracao') return;
                setEtapas((lista) => lista.map((x) => (x.id === e.id ? { ...x, ok: !x.ok } : x)));
              }}
            >
              <span className="fin-op__etapa-check">{e.ok ? '✓' : ''}</span>
              <div>
                <strong>{e.titulo}</strong>
                <p className="field-hint">{e.descricao}</p>
              </div>
            </button>
          ))}
        </div>
        <div className="fin-op__actions">
          <button type="button" className="btn-primary" onClick={fechar}>Fechar mês (RH)</button>
        </div>
      </div>
      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default FechamentoMensalRh;

import { useEffect, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';

interface ResumoMensalidade {
  valorMensalidade?: number;
  diaVencimento?: number;
  inadimplente?: boolean;
  dataUltimoPagamento?: string;
}

const PortalAlunoMensalidades: React.FC = () => {
  const [resumo, setResumo] = useState<ResumoMensalidade | null>(null);
  const [infoPagamento, setInfoPagamento] = useState('');
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregar = () => {
    HttpService.portalAlunoMensalidade().then((r) => setResumo(r.data)).catch((e) => {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    });
    HttpService.portalAlunoPagamentoInfo().then((r) => setInfoPagamento(r.data.message)).catch(() => {});
  };

  useEffect(() => { carregar(); }, []);

  return (
    <PageShell title="Mensalidades">
      {resumo && (
        <div className="card" style={{ marginBottom: '1rem' }}>
          <p><strong>Valor:</strong> R$ {resumo.valorMensalidade?.toFixed(2)}</p>
          <p><strong>Vencimento:</strong> dia {resumo.diaVencimento}</p>
          <p><strong>Situação:</strong> {resumo.inadimplente ? 'Em aberto / atrasada' : 'Em dia no mês atual'}</p>
          {resumo.dataUltimoPagamento && (
            <p><strong>Último pagamento:</strong> {resumo.dataUltimoPagamento}</p>
          )}
        </div>
      )}
      <div className="card">
        <h3 style={{ marginTop: 0 }}>Pagamento online</h3>
        <p>{infoPagamento || 'Em breve você poderá pagar por aqui.'}</p>
        <button type="button" className="btn-primary" disabled title="Integração em desenvolvimento">
          Pagar mensalidade (em breve)
        </button>
      </div>
      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default PortalAlunoMensalidades;

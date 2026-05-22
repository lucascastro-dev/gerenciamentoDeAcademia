import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import FeedbackModal from '../components/common/FeedbackModal';
import PageShell from '../components/common/PageShell';
import { carregarSessao } from '../auth/permissoes';
import HttpService from '../services/HttpService';
import { extractApiMessage } from '../utils/apiError';

const PlanoInstituicao: React.FC = () => {
  const location = useLocation();
  const planoExpirado = !!(location.state as { planoExpirado?: boolean } | null)?.planoExpirado;
  const sessao = carregarSessao();
  const instituicaoId = sessao?.vinculo || '1';
  const [assinatura, setAssinatura] = useState<any>(null);
  const [tipos, setTipos] = useState<Array<{ codigo: string; descricao: string }>>([]);
  const [planoSel, setPlanoSel] = useState('');
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregar = () => {
    HttpService.planoInstituicao(instituicaoId).then((r) => setAssinatura(r.data)).catch(() => setAssinatura(null));
    HttpService.tiposPlanoInstituicao().then((r) => setTipos(r.data)).catch(() => setTipos([]));
  };

  useEffect(() => { carregar(); }, [instituicaoId]);

  const ativar = async () => {
    if (!planoSel) return;
    try {
      await HttpService.ativarPlanoInstituicao(instituicaoId, planoSel);
      setModal({
        open: true,
        success: true,
        message: 'Plano registrado. Saia e entre novamente para liberar o acesso ao sistema.',
      });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  return (
    <PageShell
      title="Plano da instituição"
      subtitle="Assinatura da plataforma EduGestão Inteligente (teste, mensal, trimestral, semestral ou anual)"
    >
      {planoExpirado && (
        <div className="card" style={{ marginBottom: '1rem', borderColor: '#b45309', background: '#fffbeb' }}>
          <strong>Plano expirado.</strong> Renove abaixo para liberar o uso do sistema nesta instituição.
        </div>
      )}
      <div className="card" style={{ marginBottom: '1rem' }}>
        <h3 style={{ marginTop: 0 }}>Situação atual</h3>
        {assinatura?.plano ? (
          <>
            <p><strong>Plano:</strong> {assinatura.plano}</p>
            <p><strong>Vigência:</strong> {assinatura.dataInicio} até {assinatura.dataFim}</p>
            <p><strong>Status:</strong> {assinatura.vigente ? 'Ativo' : 'Expirado ou inativo'}</p>
          </>
        ) : (
          <p>Nenhuma assinatura registrada. Ative um plano abaixo.</p>
        )}
        {!sessao?.planoInstituicaoAtivo && sessao?.tipoAcesso === 'COLABORADOR' && (
          <p style={{ color: '#b45309', marginTop: '0.75rem' }}>
            O plano desta instituição não está ativo. Apenas esta tela e a home permanecem acessíveis até a renovação.
          </p>
        )}
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Ativar / renovar plano</h3>
        <label>Período</label>
        <select value={planoSel} onChange={(e) => setPlanoSel(e.target.value)}>
          <option value="">Selecione</option>
          {tipos.map((t) => (
            <option key={t.codigo} value={t.codigo}>{t.descricao}</option>
          ))}
        </select>
        <div className="form-actions form-actions--compact">
          <button type="button" className="btn-primary" onClick={ativar} disabled={!planoSel}>
            Confirmar plano
          </button>
        </div>
        <p className="field-hint">Pagamento com gateway será integrado em fase posterior; aqui registra a vigência do plano.</p>
      </div>

      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default PlanoInstituicao;

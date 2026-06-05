import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import PageShell from '../components/common/PageShell';
import { carregarSessao } from '../auth/permissoes';
import HttpService from '../services/HttpService';

const PlanoInstituicao: React.FC = () => {
  const location = useLocation();
  const planoExpirado = !!(location.state as { planoExpirado?: boolean } | null)?.planoExpirado;
  const sessao = carregarSessao();
  const instituicaoId = sessao?.vinculo && sessao.vinculo !== '0' ? sessao.vinculo : '';
  const [assinatura, setAssinatura] = useState<{
    plano?: string;
    dataInicio?: string;
    dataFim?: string;
    vigente?: boolean;
  } | null>(null);
  const [instituicao, setInstituicao] = useState<{
    razaoSocial?: string;
    statusFinanceiro?: string;
    cadastroAtivo?: boolean;
  } | null>(null);

  useEffect(() => {
    if (!instituicaoId) return;
    HttpService.planoInstituicao(instituicaoId).then((r) => setAssinatura(r.data)).catch(() => setAssinatura(null));
    HttpService.consultarInstituicao(instituicaoId).then((r) => setInstituicao(r.data)).catch(() => setInstituicao(null));
  }, [instituicaoId]);

  const labelStatusFinanceiro = (codigo?: string) => {
    switch (codigo) {
      case 'PENDENTE_PAGAMENTO': return 'Pagamento pendente';
      case 'PAGAMENTO_CONFIRMADO': return 'Pagamento confirmado';
      default: return 'Não aplicável';
    }
  };

  if (sessao?.usuarioMaster && sessao.vinculo === '0') {
    return (
      <PageShell title="Plano da instituição" subtitle="Selecione uma instituição no login para visualizar o plano vinculado.">
        <div className="card">
          <p>Operação em modo plataforma. Entre com o vínculo de uma instituição para ver a assinatura.</p>
        </div>
      </PageShell>
    );
  }

  return (
    <PageShell
      title="Plano da instituição"
      subtitle="Situação da assinatura e pagamento da sua instituição (somente consulta)."
    >
      {planoExpirado && (
        <div className="card" style={{ marginBottom: '1rem', borderColor: '#b45309', background: '#fffbeb' }}>
          <strong>Plano expirado.</strong> Entre em contato com o suporte da plataforma para renovação.
        </div>
      )}
      <div className="card" style={{ marginBottom: '1rem' }}>
        <h3 style={{ marginTop: 0 }}>Instituição</h3>
        {instituicao ? (
          <>
            <p><strong>Nome:</strong> {instituicao.razaoSocial}</p>
            <p><strong>Cadastro:</strong> {instituicao.cadastroAtivo ? 'Ativo' : 'Inativo'}</p>
            <p><strong>Status financeiro:</strong> {labelStatusFinanceiro(instituicao.statusFinanceiro)}</p>
          </>
        ) : (
          <p>Carregando dados da instituição…</p>
        )}
      </div>
      <div className="card">
        <h3 style={{ marginTop: 0 }}>Assinatura atual</h3>
        {assinatura?.plano ? (
          <>
            <p><strong>Plano:</strong> {assinatura.plano}</p>
            <p><strong>Vigência:</strong> {assinatura.dataInicio} até {assinatura.dataFim}</p>
            <p><strong>Status do plano:</strong> {assinatura.vigente ? 'Vigente' : 'Expirado ou inativo'}</p>
          </>
        ) : (
          <p>Nenhuma assinatura registrada para esta instituição.</p>
        )}
        {instituicao?.statusFinanceiro === 'PENDENTE_PAGAMENTO' && (
          <p style={{ color: '#b45309', marginTop: '0.75rem' }}>
            Pagamento pendente: o administrador tem acesso básico; demais funcionalidades liberam após confirmação pelo master da plataforma.
          </p>
        )}
        <p className="field-hint" style={{ marginTop: '1rem' }}>
          Ativação e renovação de planos são feitas pelo master da plataforma em Ativar / desativar instituição.
        </p>
      </div>
    </PageShell>
  );
};

export default PlanoInstituicao;

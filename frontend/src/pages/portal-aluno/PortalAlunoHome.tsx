import { useEffect, useState } from 'react';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { carregarSessao } from '../../auth/permissoes';

const PortalAlunoHome: React.FC = () => {
  const sessao = carregarSessao();
  const [msgPagamento, setMsgPagamento] = useState('');

  useEffect(() => {
    HttpService.portalAlunoPagamentoInfo()
      .then((r) => setMsgPagamento(r.data.message))
      .catch(() => setMsgPagamento(''));
  }, []);

  return (
    <PageShell title={`Olá, ${sessao?.nome || 'aluno'}`} subtitle="Acompanhe seus dados, turmas e mensalidades">
      <div className="card">
        <p>Use o menu acima para consultar suas informações.</p>
        <p className="field-hint">Senha inicial do portal (após matrícula): <strong>123</strong> — altere quando disponível.</p>
        {msgPagamento && (
          <p style={{ marginTop: '1rem', padding: '0.75rem', background: '#f0f9ff', borderRadius: 8 }}>{msgPagamento}</p>
        )}
      </div>
    </PageShell>
  );
};

export default PortalAlunoHome;

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import PageShell from '../components/common/PageShell';
import HttpService from '../services/HttpService';

const PagamentosPendentes: React.FC = () => {
  const [itens, setItens] = useState<Array<{ id: number; razaoSocial: string; statusFinanceiro: string }>>([]);

  useEffect(() => {
    HttpService.financeiroPlataformaResumo().then((r) => {
      const pendentes = (r.data.destaquesPendentes || []).filter(
        (i: { statusFinanceiro: string }) => i.statusFinanceiro === 'PENDENTE_PAGAMENTO',
      );
      setItens(pendentes);
    }).catch(() => setItens([]));
  }, []);

  return (
    <PageShell title="Pagamentos pendentes" subtitle="Instituições aguardando confirmação de pagamento do plano">
      <div className="card">
        {itens.length === 0 ? (
          <p className="field-hint">Nenhuma instituição com pagamento pendente no momento.</p>
        ) : (
          <ul>
            {itens.map((i) => (
              <li key={i.id} style={{ marginBottom: '0.5rem' }}>
                <strong>{i.razaoSocial}</strong> — atualize o status em{' '}
                <Link to="/arealogada/instituicoes">Consultar instituições</Link>.
              </li>
            ))}
          </ul>
        )}
      </div>
    </PageShell>
  );
};

export default PagamentosPendentes;

import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import PageShell from '../components/common/PageShell';
import HttpService from '../services/HttpService';

interface Item {
  id: number;
  razaoSocial: string;
  statusFinanceiro: string;
  plano?: string;
  planoVigente: boolean;
}

const PlanosExpirados: React.FC = () => {
  const [itens, setItens] = useState<Item[]>([]);

  useEffect(() => {
    HttpService.financeiroPlataformaResumo()
      .then((r) => setItens(r.data.destaquesPlanoExpirado || []))
      .catch(() => setItens([]));
  }, []);

  return (
    <PageShell
      title="Planos expirados"
      subtitle="Instituições sem assinatura vigente — renove em Ativar / desativar instituição"
    >
      <div className="card">
        {itens.length === 0 ? (
          <p className="field-hint">Nenhuma instituição com plano expirado no momento.</p>
        ) : (
          <ul>
            {itens.map((i) => (
              <li key={i.id} style={{ marginBottom: '0.5rem' }}>
                <strong>{i.razaoSocial}</strong>
                {i.plano ? ` · ${i.plano}` : ''}
                {' — '}
                <Link to="/arealogada/gestaoAcademia">Renovar plano</Link>
                {' · '}
                <Link to="/arealogada/instituicoes">Consultar instituições</Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </PageShell>
  );
};

export default PlanosExpirados;

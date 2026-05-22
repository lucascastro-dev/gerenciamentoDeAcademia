import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import HttpService from '../../services/HttpService';

interface DashboardFin {
  totalAlunos: number;
  receitaMensalPrevista: number;
  alunosInadimplentes: number;
  valorInadimplente: number;
}

const DashboardFinanceiro: React.FC = () => {
  const [dados, setDados] = useState<DashboardFin | null>(null);

  useEffect(() => {
    HttpService.financeiroDashboard().then((r) => {
      const { proximosVencimentos: _, ...rest } = r.data as DashboardFin & { proximosVencimentos?: unknown };
      setDados(rest);
    }).catch(() => setDados(null));
  }, []);

  if (!dados) {
    return <div className="card"><p>Carregando painel financeiro...</p></div>;
  }

  const fmt = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>Dashboard financeiro</h2>
      <p style={{ color: 'var(--color-muted)' }}>
        Indicadores de receita e inadimplência. A lista detalhada de vencimentos está em{' '}
        <Link to="/arealogada/financeiro/mensalidades">Mensalidades</Link>.
      </p>
      <div className="dashboard-grid">
        <div className="stat-card">
          <h3>{fmt(dados.receitaMensalPrevista)}</h3>
          <p>Receita mensal prevista</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#dc2626,#991b1b)' }}>
          <h3>{dados.alunosInadimplentes}</h3>
          <p>Alunos inadimplentes</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#b45309,#92400e)' }}>
          <h3>{fmt(dados.valorInadimplente)}</h3>
          <p>Valor em atraso (estimado)</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#0369a1,#075985)' }}>
          <h3>{dados.totalAlunos}</h3>
          <p>Alunos com mensalidade</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardFinanceiro;

import { useEffect, useState } from 'react';
import HttpService from '../../services/HttpService';

interface DashboardFin {
  totalAlunos: number;
  receitaMensalPrevista: number;
  alunosInadimplentes: number;
  valorInadimplente: number;
  proximosVencimentos: { nome: string; valorMensalidade: number; diaVencimento: number }[];
}

const DashboardFinanceiro: React.FC = () => {
  const [dados, setDados] = useState<DashboardFin | null>(null);

  useEffect(() => {
    HttpService.financeiroDashboard().then((r) => setDados(r.data)).catch(() => setDados(null));
  }, []);

  if (!dados) {
    return <div className="card"><p>Carregando painel financeiro...</p></div>;
  }

  const fmt = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>Dashboard financeiro</h2>
      <p style={{ color: 'var(--color-muted)' }}>Mensalidades, receita prevista e inadimplência</p>
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
      {dados.proximosVencimentos?.length > 0 && (
        <div className="card" style={{ marginTop: '1.5rem' }}>
          <h3>Próximos vencimentos</h3>
          <ul>
            {dados.proximosVencimentos.map((p, i) => (
              <li key={i}>{p.nome} — dia {p.diaVencimento} — {fmt(p.valorMensalidade)}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default DashboardFinanceiro;

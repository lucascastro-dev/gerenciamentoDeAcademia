import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { carregarSessao, isModoPlataforma } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';

interface DashboardFin {
  totalAlunos: number;
  receitaMensalPrevista: number;
  alunosInadimplentes: number;
  valorInadimplente: number;
}

interface DashboardFinPlataforma {
  instituicoesPagamentoPendente: number;
  instituicoesPlanoVencido: number;
  instituicoesComPlanoVigente: number;
  receitaPrevistaConfirmada: number;
  receitaPrevistaAguardandoPagamento: number;
}

const DashboardFinanceiro: React.FC = () => {
  const plataforma = isModoPlataforma(carregarSessao());
  const [dados, setDados] = useState<DashboardFin | null>(null);
  const [dadosPlat, setDadosPlat] = useState<DashboardFinPlataforma | null>(null);

  useEffect(() => {
    if (plataforma) {
      HttpService.financeiroPlataformaResumo()
        .then((r) => setDadosPlat(r.data))
        .catch(() => setDadosPlat(null));
    } else {
      HttpService.financeiroDashboard().then((r) => {
        const data = r.data as DashboardFin & { proximosVencimentos?: unknown };
        setDados({
          totalAlunos: data.totalAlunos,
          receitaMensalPrevista: data.receitaMensalPrevista,
          alunosInadimplentes: data.alunosInadimplentes,
          valorInadimplente: data.valorInadimplente,
        });
      }).catch(() => setDados(null));
    }
  }, [plataforma]);

  const fmt = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  if (plataforma) {
    if (!dadosPlat) return <div className="card"><p>Carregando painel financeiro da plataforma...</p></div>;
    return (
      <div>
        <h2 style={{ marginTop: 0 }}>Financeiro da plataforma</h2>
        <p style={{ color: 'var(--color-muted)' }}>
          Receitas previstas dos planos SaaS e situação de cobrança das instituições. Detalhes em{' '}
          <Link to="/arealogada/financeiro/pendentes">Pagamentos pendentes</Link>,{' '}
          <Link to="/arealogada/financeiro/planos-expirados">Planos expirados</Link> e{' '}
          <Link to="/arealogada/instituicoes">Consultar instituições</Link>.
        </p>
        <div className="dashboard-grid">
          <div className="stat-card">
            <h3>{fmt(Number(dadosPlat.receitaPrevistaConfirmada))}</h3>
            <p>Receita prevista (confirmada)</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#b45309,#92400e)' }}>
            <h3>{fmt(Number(dadosPlat.receitaPrevistaAguardandoPagamento))}</h3>
            <p>Aguardando pagamento</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#dc2626,#991b1b)' }}>
            <h3>{dadosPlat.instituicoesPagamentoPendente}</h3>
            <p>Instituições com pagamento pendente</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#7c2d12,#9a3412)' }}>
            <h3>{dadosPlat.instituicoesPlanoVencido}</h3>
            <p>Planos vencidos</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#059669,#047857)' }}>
            <h3>{dadosPlat.instituicoesComPlanoVigente}</h3>
            <p>Com plano vigente</p>
          </div>
        </div>
      </div>
    );
  }

  if (!dados) {
    return <div className="card"><p>Carregando painel financeiro...</p></div>;
  }

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>Dashboard financeiro</h2>
      <p style={{ color: 'var(--color-muted)' }}>
        Indicadores de receita e inadimplência da instituição. Detalhes em{' '}
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

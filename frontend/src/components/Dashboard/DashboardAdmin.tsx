import { useEffect, useState } from 'react';
import HttpService from '../../services/HttpService';

interface Resumo {
  totalAlunos: number;
  funcionariosAtivos: number;
  funcionariosPendentesAtivacao: number;
  totalTurmas: number;
}

const DashboardAdmin: React.FC = () => {
  const [resumo, setResumo] = useState<Resumo | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    HttpService.dashboardResumo()
      .then((r) => setResumo(r.data))
      .catch(() => setErro('Sem permissão ou falha ao carregar indicadores.'));
  }, []);

  if (erro) {
    return <div className="card"><p>{erro}</p></div>;
  }

  if (!resumo) {
    return <div className="card"><p>Carregando indicadores...</p></div>;
  }

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>Painel administrativo</h2>
      <p style={{ color: 'var(--color-muted)' }}>
        Visão geral da operação da instituição. Colaboradores ativos são quem já podem acessar o sistema;
        pendentes aguardam ativação pelo RH.
      </p>
      <div className="dashboard-grid">
        <div className="stat-card">
          <h3>{resumo.totalAlunos}</h3>
          <p>Alunos cadastrados</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#059669,#047857)' }}>
          <h3>{resumo.funcionariosAtivos}</h3>
          <p>Colaboradores ativos</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#ea580c,#c2410c)' }}>
          <h3>{resumo.funcionariosPendentesAtivacao}</h3>
          <p>Aguardando ativação</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#7c3aed,#5b21b6)' }}>
          <h3>{resumo.totalTurmas}</h3>
          <p>Turmas</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardAdmin;

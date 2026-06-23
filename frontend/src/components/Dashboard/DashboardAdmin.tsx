import { useEffect, useState } from 'react';
import { carregarSessao, isModoPlataforma } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';

interface ResumoInstituicao {
  totalAlunos: number;
  funcionariosAtivos: number;
  totalTurmas: number;
}

interface ResumoPlataforma {
  instituicoesCadastradas: number;
  instituicoesAtivas: number;
  instituicoesInativas: number;
  colaboradoresAtivos: number;
  colaboradoresPendentesAtivacao: number;
  turmasCadastradas: number;
  planosVencidos: number;
}

const DashboardAdmin: React.FC = () => {
  const sessao = carregarSessao();
  const plataforma = isModoPlataforma(sessao);
  const [resumoInst, setResumoInst] = useState<ResumoInstituicao | null>(null);
  const [resumoPlat, setResumoPlat] = useState<ResumoPlataforma | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (plataforma) {
      HttpService.dashboardPlataformaResumo()
        .then((r) => setResumoPlat(r.data))
        .catch(() => setErro('Falha ao carregar indicadores da plataforma.'));
    } else {
      HttpService.dashboardResumo()
        .then((r) => setResumoInst(r.data))
        .catch(() => setErro('Sem permissão ou falha ao carregar indicadores.'));
    }
  }, [plataforma]);

  if (erro) {
    return <div className="card"><p>{erro}</p></div>;
  }

  if (plataforma) {
    if (!resumoPlat) return <div className="card"><p>Carregando indicadores...</p></div>;
    return (
      <div>
        <div className="dashboard-grid">
          <div className="stat-card">
            <h3>{resumoPlat.instituicoesCadastradas}</h3>
            <p>Instituições cadastradas</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#059669,#047857)' }}>
            <h3>{resumoPlat.instituicoesAtivas}</h3>
            <p>Instituições ativas</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#64748b,#475569)' }}>
            <h3>{resumoPlat.instituicoesInativas}</h3>
            <p>Instituições inativas</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#0369a1,#075985)' }}>
            <h3>{resumoPlat.colaboradoresAtivos}</h3>
            <p>Colaboradores ativos (global)</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#ea580c,#c2410c)' }}>
            <h3>{resumoPlat.colaboradoresPendentesAtivacao}</h3>
            <p>Pré-cadastros pendentes</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#7c3aed,#5b21b6)' }}>
            <h3>{resumoPlat.turmasCadastradas}</h3>
            <p>Turmas (todas as instituições)</p>
          </div>
          <div className="stat-card" style={{ background: 'linear-gradient(135deg,#dc2626,#991b1b)' }}>
            <h3>{resumoPlat.planosVencidos}</h3>
            <p>Planos vencidos</p>
          </div>
        </div>
      </div>
    );
  }

  if (!resumoInst) {
    return <div className="card"><p>Carregando indicadores...</p></div>;
  }

  return (
    <div>
      <div className="dashboard-grid">
        <div className="stat-card">
          <h3>{resumoInst.totalAlunos}</h3>
          <p>Alunos cadastrados</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#059669,#047857)' }}>
          <h3>{resumoInst.funcionariosAtivos}</h3>
          <p>Colaboradores ativos</p>
        </div>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg,#7c3aed,#5b21b6)' }}>
          <h3>{resumoInst.totalTurmas}</h3>
          <p>Turmas</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardAdmin;

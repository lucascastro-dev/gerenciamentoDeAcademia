import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { carregarSessao, isPortalAluno } from '../../auth/permissoes';

const ROTAS_LIBERADAS = ['/arealogada/home', '/arealogada/plano-instituicao', '/arealogada/meu-perfil'];

/**
 * Redireciona colaboradores para renovação de plano quando a instituição está sem assinatura ativa.
 */
const PlanoInstituicaoGuard = () => {
  const sessao = carregarSessao();
  const location = useLocation();
  const path = location.pathname;

  if (!sessao || isPortalAluno(sessao) || sessao.usuarioMaster || sessao.planoInstituicaoAtivo !== false) {
    return <Outlet />;
  }

  const liberada = ROTAS_LIBERADAS.some((r) => path === r || path.startsWith(`${r}/`));
  if (liberada) {
    return <Outlet />;
  }

  return <Navigate to="/arealogada/plano-instituicao" replace state={{ planoExpirado: true }} />;
};

export default PlanoInstituicaoGuard;

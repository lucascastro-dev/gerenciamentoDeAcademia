import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { carregarSessao, isPortalAluno } from './auth/permissoes';

const ProtectedRoute = () => {
  const token = localStorage.getItem('@App:token');
  const sessao = carregarSessao();
  const location = useLocation();

  if (!token) {
    return <Navigate to="/areapublica/login" replace />;
  }

  const aluno = isPortalAluno(sessao);
  const path = location.pathname;

  if (path.startsWith('/portal-aluno')) {
    const destino = path.replace(/^\/portal-aluno/, '/arealogada/aluno') || '/arealogada/home';
    return <Navigate to={destino === '/arealogada/aluno' ? '/arealogada/home' : destino} replace />;
  }

  const ehRotaPortalAluno = /^\/arealogada\/aluno(\/|$)/.test(path);

  if (aluno && path.startsWith('/arealogada')) {
    const permitido = path === '/arealogada/home' || ehRotaPortalAluno;
    if (!permitido) {
      return <Navigate to="/arealogada/home" replace />;
    }
  }

  if (!aluno && ehRotaPortalAluno) {
    return <Navigate to="/arealogada/home" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;

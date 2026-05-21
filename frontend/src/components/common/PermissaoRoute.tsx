import { Navigate } from 'react-router-dom';
import { carregarSessao, possuiPermissao } from '../../auth/permissoes';

interface Props {
  permissao?: string;
  perfis?: string[];
  somenteMaster?: boolean;
  children: React.ReactNode;
}

const PermissaoRoute: React.FC<Props> = ({ permissao, perfis, somenteMaster, children }) => {
  const sessao = carregarSessao();
  if (!sessao) {
    return <Navigate to="/areapublica/login" replace />;
  }
  if (somenteMaster && !sessao.usuarioMaster) {
    return <Navigate to="/arealogada/home" replace />;
  }
  if (perfis?.length && sessao.tipoFuncionario && perfis.includes(sessao.tipoFuncionario)) {
    return <>{children}</>;
  }
  if (permissao && !possuiPermissao(sessao, permissao)) {
    return <Navigate to="/arealogada/home" replace />;
  }
  return <>{children}</>;
};

export default PermissaoRoute;

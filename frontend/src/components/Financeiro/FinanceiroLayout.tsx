import { NavLink, Outlet } from 'react-router-dom';
import { carregarSessao, isModoPlataforma, possuiPermissao } from '../../auth/permissoes';

const linkStyle = { textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 };

const FinanceiroLayout: React.FC = () => {
  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);
  const pode = (codigo: string) => possuiPermissao(sessao, codigo);

  return (
    <div>
      <nav style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem', flexWrap: 'wrap' }}>
        <NavLink to="/arealogada/financeiro" end className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
          Dashboard
        </NavLink>
        {master && (
          <>
            <NavLink to="/arealogada/financeiro/pendentes" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
              Pagamentos pendentes
            </NavLink>
            <NavLink to="/arealogada/financeiro/planos-expirados" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
              Planos expirados
            </NavLink>
          </>
        )}
        {!master && pode('financeiro:visualizar') && (
          <NavLink to="/arealogada/financeiro/mensalidades" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
            Mensalidades
          </NavLink>
        )}
        {!master && pode('financeiro:relatorio') && (
          <NavLink to="/arealogada/financeiro/inadimplencia" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
            Inadimplências
          </NavLink>
        )}
        {!master && pode('financeiro:cobranca') && (
          <NavLink to="/arealogada/financeiro/folha-pagamento" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
            Folha
          </NavLink>
        )}
        {!master && pode('financeiro:visualizar') && (
          <NavLink to="/arealogada/financeiro/conciliacao" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
            Conciliação
          </NavLink>
        )}
        {!master && pode('financeiro:relatorio') && (
          <NavLink to="/arealogada/financeiro/fechamento-mes" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={linkStyle}>
            Fechamento
          </NavLink>
        )}
      </nav>
      <Outlet />
    </div>
  );
};

export default FinanceiroLayout;

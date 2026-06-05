import { NavLink, Outlet } from 'react-router-dom';
import { carregarSessao, isModoPlataforma } from '../../auth/permissoes';

const FinanceiroLayout: React.FC = () => {
  const master = isModoPlataforma(carregarSessao());

  return (
    <div>
      <nav style={{ display: 'flex', gap: '1rem', marginBottom: '1.25rem', flexWrap: 'wrap' }}>
        <NavLink
          to="/arealogada/financeiro"
          end
          className={({ isActive }) => (isActive ? 'btn-primary' : '')}
          style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}
        >
          Dashboard
        </NavLink>
        {master ? (
          <NavLink
            to="/arealogada/financeiro/pendentes"
            className={({ isActive }) => (isActive ? 'btn-primary' : '')}
            style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}
          >
            Pagamentos pendentes
          </NavLink>
        ) : null}
        {master ? (
          <NavLink
            to="/arealogada/financeiro/planos-expirados"
            className={({ isActive }) => (isActive ? 'btn-primary' : '')}
            style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}
          >
            Planos expirados
          </NavLink>
        ) : null}
        {!master ? (
          <>
            <NavLink
              to="/arealogada/financeiro/mensalidades"
              className={({ isActive }) => (isActive ? 'btn-primary' : '')}
              style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}
            >
              Mensalidades
            </NavLink>
            <NavLink
              to="/arealogada/financeiro/inadimplencia"
              className={({ isActive }) => (isActive ? 'btn-primary' : '')}
              style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}
            >
              Inadimplência
            </NavLink>
          </>
        ) : null}
      </nav>
      <Outlet />
    </div>
  );
};

export default FinanceiroLayout;

import { NavLink, Outlet } from 'react-router-dom';

const FinanceiroLayout: React.FC = () => (
  <div>
    <nav style={{ display: 'flex', gap: '1rem', marginBottom: '1.25rem', flexWrap: 'wrap' }}>
      <NavLink to="/arealogada/financeiro" end className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}>
        Dashboard
      </NavLink>
      <NavLink to="/arealogada/financeiro/mensalidades" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}>
        Mensalidades
      </NavLink>
      <NavLink to="/arealogada/financeiro/inadimplencia" className={({ isActive }) => (isActive ? 'btn-primary' : '')} style={{ textDecoration: 'none', padding: '0.5rem 1rem', borderRadius: 8 }}>
        Inadimplência
      </NavLink>
    </nav>
    <Outlet />
  </div>
);

export default FinanceiroLayout;

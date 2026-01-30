import { Navigate, Outlet } from 'react-router-dom';

const ProtectedRoute = () => {
  const token = localStorage.getItem('@App:token');

  if (!token) {
    return <Navigate to="/areapublica/login" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;
// src/components/Login.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import { Link } from 'react-router-dom';
import { AxiosError } from 'axios';

const Login: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [token, setToken] = useState('');
  const [state, setState] = useState('');
  const navigate = useNavigate();

  const handleLogin = async () => {
    await AuthService.login(login, password).then((value) => {
      setToken(value.data);
      navigate('/home');
    }).catch((value: AxiosError) => {
      setState(value.response ? `${value.response.data}` : `${value}`)
    });;
  };

  console.log("Token gerado: " + token)

  useEffect(() => {
    console.log(state);
  }, [state])

  return (
    <div>
      <h2>Login</h2>
      <form>
        <label>
          <input
            type="text"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
            placeholder="Digite seu usuário"
          />
        </label>
        <br />
        <label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Digite sua senha"
          />
        </label>
        <br />
        <button type="button" onClick={handleLogin}>
          Login
        </button>
      </form>
      <Link to="/arealogada/cadastro">Registrar</Link>
    </div>
  );
};

export default Login;

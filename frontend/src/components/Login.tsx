// src/components/Login.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import { Link } from 'react-router-dom';
import { AxiosError } from 'axios';
import "./Login.css"

const Login: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [codAcademia, setCodAcademia] = useState('');
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

const isFormValid =
  login !== "" &&
  password !== "" &&
  codAcademia !== "";

  useEffect(() => {
    console.log(state);
  }, [state])

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form>
        <input
          type="text"
          value={codAcademia}
          onChange={(e) => setCodAcademia(e.target.value)}
          placeholder="Digite o código da academia"
        />
        <input
          type="text"
          value={login}
          onChange={(e) => setLogin(e.target.value)}
          placeholder="Digite seu usuário (CPF/Email)"
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Digite sua senha"
        />
        <button disabled={!isFormValid} type="button" onClick={handleLogin}>
          Login
        </button>
      </form>
      <Link to="/arealogada/cadastro">Registrar</Link>
    </div>
  );

};

export default Login;

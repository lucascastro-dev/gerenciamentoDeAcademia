import { AxiosError } from 'axios';
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HttpService from '../../services/HttpService';
import "./Login.css";

const Login: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [vinculo, setVinculo] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleLoginMask = (value: string) => {
    if (/^\d/.test(value)) {
      return value
        .replace(/\D/g, "")
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d{1,2})$/, "$1-$2")
        .slice(0, 14);
    }
    return value;
  };

  const sanitizeCpf = (value: string) => {
  return /^\d/.test(value) ? value.replace(/\D/g, "") : value;
};

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg(null);

    const cleanLogin = sanitizeCpf(login);

    try {
      const response = await HttpService.login(cleanLogin, password, vinculo);
      
      const { token } = response.data;

      localStorage.setItem('@App:token', token);
      localStorage.setItem('@App:cpf', cleanLogin);
      localStorage.setItem('@App:vinculo', vinculo);

      navigate('/arealogada/home');
    } catch (err) {
      const axiosError = err as AxiosError;

      const data = axiosError.response?.data as any;

      const message =
        data?.message ||
        data?.error ||
        data?.msg ||
        (typeof data === 'string' ? data : null) ||
        "Erro inesperado no servidor";
      setErrorMsg(message);
    } finally {
      setLoading(false);
    }
  };

  const isFormValid = login.length > 0 && password.length > 0 && vinculo.length > 0 && !loading;

  return (
    <div className="main-wrapper">
      <div className='header'>Gestão de Academias Inteligente</div>

      <div className="login-container">
        <h2>Login</h2>

        <form onSubmit={handleLogin}>
          <input
            type="text"
            placeholder="Código da academia"
            value={vinculo}
            onChange={(e) => setVinculo(e.target.value)}
          />

          <input
            type="text"
            placeholder="Digite seu usuário (CPF ou Email)"
            value={login}
            onChange={(e) => setLogin(handleLoginMask(e.target.value))}
          />

          <input
            type="password"
            placeholder="Digite sua senha"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button disabled={!isFormValid} type="submit">
            {loading ? "Carregando..." : "Login"}
          </button>
        </form>

        <div className="links-container">
          <Link to="/areapublica/cadastro">Registrar</Link>
          <Link to="/areapublica/esqueciSenha">Esqueci minha senha</Link>
          <Link to="/areapublica/solicitarAcesso">Realizar primeiro acesso</Link>
        </div>
      </div>

      {errorMsg && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Ops! Algo deu errado</h3>
            <p>{errorMsg}</p>
            <button onClick={() => setErrorMsg(null)}>Fechar</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Login;
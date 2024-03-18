// src/components/Login.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import { Link } from 'react-router-dom';

const Login: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const token = await AuthService.login(login, password);
      // Salvar token no armazenamento local ou de sessão conforme necessário
      // Exemplo: localStorage.setItem('token', token);
      navigate('/dashboard');
    } catch (error) {
      console.error('Erro de login:', error);
      // Lidar com erros de login, como exibição de uma mensagem de erro
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <form>
        <label>
          Usuário:
          <input
            type="text"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
            placeholder="Digite seu usuário"
          />
        </label>
        <br />
        <label>
          Senha:
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
      <Link to="/cadastro">Registrar</Link>
    </div>
  );
};

export default Login;

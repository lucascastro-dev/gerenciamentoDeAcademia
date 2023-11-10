import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';

const Cadastro: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const navigate = useNavigate();

  const handleCadastro = async () => {
    try {
      await AuthService.cadastrar({ login, password, role });
      // Após o cadastro bem-sucedido, redirecione para a tela de login ou outra página
      navigate('/login');
    } catch (error) {
      console.error('Erro ao cadastrar:', error);
    }
  };

  return (
    <div>
      <h2>Cadastro</h2>
      <label>
        Login:
        <input type="text" value={login} onChange={(e) => setLogin(e.target.value)} />
      </label>
      <br />
      <label>
        Senha:
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      </label>
      <br />
      <label>
        Role:
        <select value={role} onChange={(e) => setRole(e.target.value)}>
          <option value="USER">Usuário</option>
          <option value="ADMIN">Administrador</option>
        </select>
      </label>
      <br />
      <button type="button" onClick={handleCadastro}>
        Cadastrar
      </button>
    </div>
  );
};

export default Cadastro;

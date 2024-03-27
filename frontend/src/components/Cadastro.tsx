import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import { AxiosError } from 'axios';

const Cadastro: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [state, setState] = useState('');
  const navigate = useNavigate();

  const handleCadastro = async () => {
    await AuthService.cadastrar({ login, password, role })
      .then((value) => {
        setState(value.data)
        navigate('/arealogada/login');
      })
      .catch((value: AxiosError) => {
        setState(value.response ? `${value.response.data}` : `${value}`)
      });

  };

  useEffect(() => {
    console.log(state);
  }, [state])

  return (
    <div>
      <h2>Cadastro</h2>
      <label>
        <input placeholder='Usuário' type="text" value={login} onChange={(e) => setLogin(e.target.value)} />
      </label>
      <br />
      <label>
        <input placeholder='Senha' type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      </label>
      <br />
      <label>
        <h5>Tipo de usuário</h5>
        <select value={role} onChange={(e) => setRole(e.target.value)}>
          <option value="USER">Usuário</option>
          <option value="ADMIN">Administrador</option>
        </select>
      </label>
      <br />
      <button type="button" onClick={handleCadastro}>
        Cadastrar
      </button>
<br/>
      <Link to="/arealogada/login">Voltar</Link>
    </div>
  );
};

export default Cadastro;

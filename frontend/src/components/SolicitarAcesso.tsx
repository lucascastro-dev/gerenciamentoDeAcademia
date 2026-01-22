import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import { AxiosError } from 'axios';
import "./Login.css"

const SolicitarAcesso: React.FC = () => {
  const [cnpj, setCnpj] = useState('');
  const [cpf, setCpf] = useState('');
  const [state, setState] = useState('');
  const navigate = useNavigate();

  const handleSolicitarPrimeiroAcesso = async () => {
    await AuthService.vincularFuncionario({ cnpj, cpf })
      .then((value) => {
        setState(value.data)
        navigate('/arealogada/login');
      })
      .catch((value: AxiosError) => {
        setState(value.response ? `${value.response.data}` : `${value}`)
      });
  };

  const isFormValid =
    cpf !== "" && cnpj !== "" ? true : false;

  useEffect(() => {
    console.log(state);
  }, [state])

  return (
    <div className="login-container">
      <h2>Solicitar primeiro acesso</h2>
      <form>
        <input
          placeholder="CNPJ Academia"
          type="text"
          value={cnpj}
          onChange={(e) => setCnpj(e.target.value)}
        />
        <input
          placeholder="CPF Funcionário"
          type="text"
          value={cpf}
          onChange={(e) => setCpf(e.target.value)}
        />

        <button
          type="button"
          onClick={handleSolicitarPrimeiroAcesso}
          disabled={!isFormValid}
        >
          Solicitar
        </button>

      </form>

      <Link to="/arealogada/login">Voltar</Link>
    </div>
  );
}

export default SolicitarAcesso;

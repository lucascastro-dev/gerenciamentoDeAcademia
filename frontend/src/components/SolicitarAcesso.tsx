import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HttpService from '../services/HttpService';
import { AxiosError } from 'axios';
import "./Login.css"

const SolicitarAcesso: React.FC = () => {
  const [cnpj, setCnpj] = useState('');
  const [cpf, setCpf] = useState('');
  const [state, setState] = useState('');
  const navigate = useNavigate();

  const handleSolicitarPrimeiroAcesso = async () => {
    await HttpService.vincularFuncionario({ cnpj, cpf })
      .then((value) => {
        setState(value.data)
        navigate('/arealogada/login');
      })
      .catch((value: AxiosError) => {
        setState(value.response ? `${value.response.data}` : `${value}`)
      });
  };

    const maskCPF = (value: string) => {
    return value
      .replace(/\D/g, "")
      .replace(/(\d{3})(\d)/, "$1.$2")
      .replace(/(\d{3})(\d)/, "$1.$2")
      .replace(/(\d{3})(\d{1,2})$/, "$1-$2")
      .slice(0, 14);
  };

  const maskCNPJ = (value: string) => {
    return value
      .toUpperCase()
      .replace(/[^A-Z0-9]/g, "")
      .replace(/^([A-Z0-9]{2})([A-Z0-9])/, "$1.$2")
      .replace(/^([A-Z0-9]{2})\.([A-Z0-9]{3})([A-Z0-9])/, "$1.$2.$3")
      .replace(/\.([A-Z0-9]{3})([A-Z0-9])/, ".$1/$2")
      .replace(/([A-Z0-9]{4})([A-Z0-9])/, "$1-$2")
      .slice(0, 18);
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
          onChange={(e) => setCnpj(maskCNPJ(e.target.value))}
        />
        <input
          placeholder="CPF Funcionário"
          type="text"
          value={cpf}
          onChange={(e) => setCpf(maskCPF(e.target.value))}
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

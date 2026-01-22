import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/AuthService';
import { AxiosError } from 'axios';
import "./Login.css"

const Cadastro: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [razaoSocial, setRazaoSocial] = useState('');
  const [endereco, setEndereco] = useState('');
  const [telefone, setTelefone] = useState('');
  const [role, setRole] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataNascimento, setNascimento] = useState('');
  const [cargo, setCargo] = useState('');
  const [especializacao, setEspecializacao] = useState('');
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

  const isFormValid =
    role === "ADMIN"
      ? login !== "" && password !== "" && razaoSocial !== "" && endereco !== "" && telefone !== ""
      : role === "USER"
        ? login !== "" && password !== "" && nome !== "" && rg !== "" && dataNascimento !== "" && cargo !== "" && especializacao !== "" && endereco !== "" && telefone !== ""
        : false;

  useEffect(() => {
    console.log(state);
  }, [state])

  return (
    <div className="login-container">
      <h2>Cadastro</h2>
      <form>
        <h5>Tipo de cadastro</h5>
        <select value={role} onChange={(e) => setRole(e.target.value)}>
          <option value="USER">Funcionário</option>
          <option value="ADMIN">Academia</option>
        </select>

        {role === "ADMIN" && (
          <input
            placeholder="Razão Social"
            type="text"
            value={razaoSocial}
            onChange={(e) => setRazaoSocial(e.target.value)}
          />
        )}

        {role === "USER" && (
          <input
            placeholder="Nome"
            type="text"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
          />
        )}

        {role === "ADMIN" && (
          <input
            placeholder="CNPJ"
            type="text"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
          />)}

        {role === "USER" && (
          <input
            placeholder="CPF"
            type="text"
            value={login}
            onChange={(e) => setLogin(e.target.value)}
          />
        )}

        {role === "USER" && (
          <input
            placeholder="RG"
            type="text"
            value={rg}
            onChange={(e) => setRg(e.target.value)}
          />
        )}

        {role === "USER" && (
          <input
            placeholder="Data de Nascimento"
            type="date"
            value={dataNascimento}
            onChange={(e) => setNascimento(e.target.value)}
          />
        )}

        {role === "USER" && (
          <input
            placeholder="Cargo"
            type="text"
            value={cargo}
            onChange={(e) => setCargo(e.target.value)}
          />
        )}

        {role === "USER" && (
          <input
            placeholder="Especialização"
            type="text"
            value={especializacao}
            onChange={(e) => setEspecializacao(e.target.value)}
          />
        )}

        {role === "USER" && (
          <input
            placeholder="Especialização"
            type="text"
            value={especializacao}
            onChange={(e) => setEspecializacao(e.target.value)}
          />
        )}

        {role !== "" && (
          <input
            placeholder="Endereço"
            type="text"
            value={endereco}
            onChange={(e) => setEndereco(e.target.value)}
          />)}

        {role !== "" && (
          <input
            placeholder="Telefone"
            type="phone"
            value={telefone}
            onChange={(e) => setTelefone(e.target.value)}
          />)}

        {login !== "" && (
          <input
            placeholder="Senha"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />)}

        <button
          type="button"
          onClick={handleCadastro}
          disabled={!isFormValid}
        >
          Cadastrar
        </button>

      </form>

      <Link to="/arealogada/login">Voltar</Link>
    </div>
  );
}

export default Cadastro;

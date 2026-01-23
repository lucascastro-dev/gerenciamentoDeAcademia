import { AxiosError } from 'axios';
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HttpService from '../services/HttpService';
import "./Login.css";

const Cadastro: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [razaoSocial, setRazaoSocial] = useState('');
  const [endereco, setEndereco] = useState('');
  const [telefone, setTelefone] = useState('');
  const [role, setRole] = useState('ADMIN');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setNascimento] = useState('');
  const [cargo, setCargo] = useState('');
  const [especializacao, setEspecializacao] = useState('');

  const [modal, setModal] = useState<{ show: boolean, message: string, isSuccess: boolean }>({
    show: false,
    message: '',
    isSuccess: false
  });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const maskCPF = (v: string) => v.replace(/\D/g, "").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2").slice(0, 14);
  const maskCNPJ = (v: string) => v.toUpperCase().replace(/[^A-Z0-9]/g, "").replace(/^([A-Z0-9]{2})([A-Z0-9])/, "$1.$2").replace(/^([A-Z0-9]{2})\.([A-Z0-9]{3})([A-Z0-9])/, "$1.$2.$3").replace(/\.([A-Z0-9]{3})([A-Z0-9])/, ".$1/$2").replace(/([A-Z0-9]{4})([A-Z0-9])/, "$1-$2").slice(0, 18);
  const maskPhone = (v: string) => v.replace(/\D/g, "").replace(/^(\d{2})(\d)/g, "($1) $2").replace(/(\d)(\d{4})$/, "$1-$2").slice(0, 15);
  const isPasswordValid = (p: string) => /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(p);
  const onlyNumbers = (v: string) => v.replace(/\D/g, "");
  const sanitizeCNPJ = (v: string) => v.replace(/[./-]/g, "");

  const isFormValid = role === "ADMIN"
    ? login !== "" && razaoSocial !== "" && endereco !== "" && telefone !== ""
    : login !== "" && isPasswordValid(password) && nome !== "" && rg !== "" && dataDeNascimento !== "" && cargo !== "" && especializacao !== "" && endereco !== "" && telefone !== "";

  const getErrorMessage = (err: any) => {
    const axiosError = err as AxiosError;

    console.log(axiosError.response?.data)

    if (axiosError.response?.data) {
      const data = axiosError.response.data;

      if (typeof data === 'string') return data;

      if (typeof data === 'object') {
        const anyData = data as any;
        return anyData.message || anyData.error || "Erro ao processar requisição.";
      }
    }

    if (axiosError.request) {
      return "Não foi possível conectar ao servidor. Verifique sua conexão.";
    }

    return "Ocorreu um erro inesperado.";
  };

  const handleFinish = async (request: Promise<any>) => {
    setLoading(true);
    try {
      await request;
      setModal({ show: true, message: "Cadastro realizado com sucesso!", isSuccess: true });
    } catch (err) {
      console.log(err)
      setModal({ show: true, message: getErrorMessage(err), isSuccess: false });
    } finally {
      setLoading(false);
    }
  };

  const handleCadastroEmpresa = () => {
    handleFinish(
      HttpService.cadastrarEmpresa({
        razaoSocial,
        cnpj: sanitizeCNPJ(login),
        cadastroAtivo: false,
        endereco,
        telefone: onlyNumbers(telefone),
      })
    );
  };

  const handleCadastroPessoa = () => {
    handleFinish(
      HttpService.cadastrarPessoa({
        nome,
        cpf: onlyNumbers(login),
        rg,
        dataDeNascimento,
        endereco,
        telefone: onlyNumbers(telefone),
        cargo,
        especializacao,
        permitirGerenciarFuncoes: false,
        senha: password,
      })
    );
  };
  const closeModal = () => {
    if (modal.isSuccess) navigate('/arealogada/login');
    setModal({ ...modal, show: false });
  };

  return (
    <div className="login-container">
      <h2>Cadastro</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <h5>Tipo de cadastro</h5>
        <select value={role} onChange={(e) => { setRole(e.target.value); setLogin(""); }}>
          <option value="USER">Funcionário</option>
          <option value="ADMIN">Academia</option>
        </select>

        {role === "ADMIN" ? (
          <>
            <input placeholder="Razão Social" value={razaoSocial} onChange={(e) => setRazaoSocial(e.target.value)} />
            <input placeholder="CNPJ" value={login} onChange={(e) => setLogin(maskCNPJ(e.target.value))} />
          </>
        ) : (
          <>
            <input placeholder="Nome" value={nome} onChange={(e) => setNome(e.target.value)} />
            <input placeholder="CPF" value={login} onChange={(e) => setLogin(maskCPF(e.target.value))} />
            <input placeholder="RG" value={rg} onChange={(e) => setRg(e.target.value)} />
            <input type="date" value={dataDeNascimento} onChange={(e) => setNascimento(e.target.value)} />
            <input placeholder="Cargo" value={cargo} onChange={(e) => setCargo(e.target.value)} />
            <input placeholder="Especialização" value={especializacao} onChange={(e) => setEspecializacao(e.target.value)} />
            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <input type="password" placeholder="Senha" value={password} onChange={(e) => setPassword(e.target.value)} style={{ borderColor: password && !isPasswordValid(password) ? 'red' : '' }} />
              {password && !isPasswordValid(password) && (
                <span style={{ color: 'red', fontSize: '10px' }}>Senha fraca: Requer 8+ chars, Maiúscula, Minúscula, Número e Especial.</span>
              )}
            </div>
          </>
        )}

        <input placeholder="Endereço" value={endereco} onChange={(e) => setEndereco(e.target.value)} />
        <input placeholder="Telefone" type="tel" value={telefone} onChange={(e) => setTelefone(maskPhone(e.target.value))} />

        <button type="button" onClick={role === "USER" ? handleCadastroPessoa : handleCadastroEmpresa} disabled={!isFormValid || loading}>
          {loading ? "Processando..." : "Cadastrar"}
        </button>
      </form>
      <Link to="/arealogada/login">Voltar</Link>

      {modal.show && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ color: modal.isSuccess ? '#2e7d32' : '#d32f2f' }}>{modal.isSuccess ? 'Sucesso!' : 'Erro inesperado!'}</h3>
            <p>{modal.message}</p>
            <button className={modal.isSuccess ? 'btn-success' : 'btn-error'} onClick={closeModal}>
              {modal.isSuccess ? 'Fechar' : 'Tentar novamente'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cadastro;
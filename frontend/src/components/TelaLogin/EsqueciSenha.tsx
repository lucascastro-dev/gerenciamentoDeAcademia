import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HttpService from '../../services/HttpService';
import { AxiosError } from 'axios';
import "./Login.css";

const EsqueciSenha: React.FC = () => {
  const [cnpj, setCnpj] = useState('');
  const [cpf, setCpf] = useState('');
  const [loading, setLoading] = useState(false);
  
  const [modal, setModal] = useState<{ show: boolean, message: string, isSuccess: boolean }>({
    show: false,
    message: '',
    isSuccess: false
  });

  const navigate = useNavigate();

  const maskCPF = (v: string) => v.replace(/\D/g, "").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2").slice(0, 14);
  const sanitize = (v: string) => v.replace(/[./-]/g, "");

  const getErrorMessage = (err: any) => {
    const axiosError = err as AxiosError;
    const data = axiosError.response?.data;
    if (typeof data === 'string') return data;
    if (data && typeof data === 'object') return (data as any).message || (data as any).error || "Erro na solicitação.";
    return "Erro de conexão com o servidor.";
  };

  const handleSolicitarPrimeiroAcesso = async () => {
    setLoading(true);
    try {
      await HttpService.vincularFuncionario({ 
        cnpj: sanitize(cnpj), 
        cpf: sanitize(cpf) 
      });
      
      setModal({ 
        show: true, 
        message: "Em breve você receberá as instruções no seu e-mail para resetar sua senha!", 
        isSuccess: true 
      });
    } catch (err) {
      setModal({ 
        show: true, 
        message: getErrorMessage(err), 
        isSuccess: false 
      });
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    if (modal.isSuccess) navigate('/areapublica/login');
    setModal({ ...modal, show: false });
  };

  const isFormValid = cpf.length >= 14 && cnpj.length >= 1 && !loading;

  return (
    <div className="login-container">
      <h2>Recuperar senha de login</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <input
          placeholder="Código academia"
          type="text"
          value={cnpj}
          onChange={(e) => setCnpj(e.target.value)}
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
          {loading ? "Enviando..." : "Recuperar Senha"}
        </button>
      </form>

      <Link to="/areapublica/login">Voltar</Link>

      {modal.show && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ color: modal.isSuccess ? '#2e7d32' : '#d32f2f' }}>
              {modal.isSuccess ? 'Sucesso!' : 'Erro'}
            </h3>
            <p>{modal.message}</p>
            <button 
              className={modal.isSuccess ? 'btn-success' : 'btn-error'} 
              onClick={closeModal}
            >
              {modal.isSuccess ? 'Fechar' : 'Tentar novamente'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default EsqueciSenha;
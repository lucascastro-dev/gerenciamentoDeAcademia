import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import './Login.css';

const SolicitarAcesso: React.FC = () => {
  const [cpf, setCpf] = useState('');
  const [loading, setLoading] = useState(false);

  const [modal, setModal] = useState<{ show: boolean; message: string; isSuccess: boolean }>({
    show: false,
    message: '',
    isSuccess: false,
  });

  const navigate = useNavigate();

  const maskCPF = (v: string) =>
    v.replace(/\D/g, '').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2').slice(0, 14);

  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  const handleSolicitar = async () => {
    setLoading(true);
    try {
      await HttpService.solicitarPrimeiroAcesso(onlyNumbers(cpf));
      setModal({
        show: true,
        message:
          'Solicitação registrada. O RH da instituição consultará seu CPF e ativará seu vínculo. Você será avisado quando puder entrar.',
        isSuccess: true,
      });
    } catch (err) {
      setModal({
        show: true,
        message: extractApiMessage(err, 'Erro na solicitação.'),
        isSuccess: false,
      });
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    if (modal.isSuccess) navigate('/areapublica/login');
    setModal({ show: false, message: '', isSuccess: false });
  };

  const isFormValid = cpf.replace(/\D/g, '').length >= 11 && !loading;

  return (
    <div className="login-container">
      <h2>Solicitar ativação</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <p style={{ fontSize: '0.85rem', color: '#64748b' }}>
          Já fez o pré-cadastro? Informe seu CPF. O RH da instituição em que você trabalhará irá ativar seu acesso
          (não é necessário informar CNPJ).
        </p>
        <input
          placeholder="CPF"
          type="text"
          value={cpf}
          onChange={(e) => setCpf(maskCPF(e.target.value))}
        />

        <button type="button" onClick={handleSolicitar} disabled={!isFormValid}>
          {loading ? 'Enviando...' : 'Solicitar'}
        </button>
      </form>

      <Link to="/areapublica/login">Voltar</Link>
      <Link to="/areapublica/cadastro" style={{ display: 'block', marginTop: 8 }}>
        Ainda não tenho pré-cadastro
      </Link>

      {modal.show && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ color: modal.isSuccess ? '#2e7d32' : '#d32f2f' }}>
              {modal.isSuccess ? 'Sucesso!' : 'Erro'}
            </h3>
            <p>{modal.message}</p>
            <button type="button" onClick={closeModal}>
              {modal.isSuccess ? 'Fechar' : 'Tentar novamente'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default SolicitarAcesso;

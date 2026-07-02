import React, { useState } from 'react';
import { PLACEHOLDERS } from '../../constants/copy';
import { Link, useNavigate } from 'react-router-dom';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import LogoMark from '../common/LogoMark';
import './Login.css';

const EsqueciSenha: React.FC = () => {
  const [cpf, setCpf] = useState('');
  const [loading, setLoading] = useState(false);

  const [modal, setModal] = useState<{ show: boolean; message: string; isSuccess: boolean }>({
    show: false,
    message: '',
    isSuccess: false,
  });

  const navigate = useNavigate();

  const maskCPF = (v: string) =>
    v
      .replace(/\D/g, '')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
      .slice(0, 14);

  const cpfLimpo = cpf.replace(/\D/g, '');

  const handleRecuperar = async () => {
    setLoading(true);
    try {
      const { data } = await HttpService.solicitarRecuperacaoSenha(cpfLimpo);
      setModal({
        show: true,
        message: data.message
          || 'Solicitação registrada. Em breve você receberá um e-mail com instruções para redefinir sua senha.',
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
    if (modal.isSuccess) navigate('/entrar');
    setModal({ ...modal, show: false });
  };

  const isFormValid = cpfLimpo.length >= 11 && !loading;

  return (
    <div className="auth-page">
      <LogoMark className="auth-page__brand" />
      <div className="login-container">
        <h2>Recuperar senha</h2>
        <p className="login-hint">
          Informe o CPF cadastrado na plataforma. Se houver vínculo ativo com alguma instituição,
          registraremos sua solicitação de redefinição de senha.
        </p>
        <form onSubmit={(e) => e.preventDefault()}>
          <label className="login-label">CPF</label>
          <input
            placeholder={PLACEHOLDERS.cpf}
            type="text"
            inputMode="numeric"
            value={cpf}
            onChange={(e) => setCpf(maskCPF(e.target.value))}
          />

          <button type="button" className="auth-btn-primary" onClick={handleRecuperar} disabled={!isFormValid}>
            {loading ? 'Enviando...' : 'Recuperar senha'}
          </button>
        </form>

        <Link to="/areapublica/login">Voltar ao login</Link>

        {modal.show && (
          <div className="modal-overlay">
            <div className="modal-content">
              <h3 style={{ color: modal.isSuccess ? '#2e7d32' : '#d32f2f' }}>
                {modal.isSuccess ? 'Solicitação registrada' : 'Erro'}
              </h3>
              <p>{modal.message}</p>
              <button
                type="button"
                className={modal.isSuccess ? 'btn-success' : 'btn-error'}
                onClick={closeModal}
              >
                {modal.isSuccess ? 'Fechar' : 'Tentar novamente'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default EsqueciSenha;

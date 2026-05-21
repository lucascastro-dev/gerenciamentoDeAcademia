import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import './Login.css';

const Cadastro: React.FC = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [endereco, setEndereco] = useState('');
  const [telefone, setTelefone] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setNascimento] = useState('');

  const [modal, setModal] = useState<{ show: boolean; message: string; isSuccess: boolean }>({
    show: false,
    message: '',
    isSuccess: false,
  });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const maskCPF = (v: string) =>
    v.replace(/\D/g, '').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2').slice(0, 14);
  const maskPhone = (v: string) =>
    v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d)(\d{4})$/, '$1-$2').slice(0, 15);
  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  const isFormValid =
    login.length >= 14 &&
    password.length >= 4 &&
    nome !== '' &&
    rg !== '' &&
    dataDeNascimento !== '' &&
    endereco !== '' &&
    telefone !== '';

  const handleCadastro = async () => {
    setLoading(true);
    try {
      await HttpService.preCadastroColaborador({
        nome,
        cpf: onlyNumbers(login),
        rg,
        dataDeNascimento,
        endereco,
        telefone: onlyNumbers(telefone),
        senha: password,
      });
      setModal({
        show: true,
        message:
          'Pré-cadastro enviado! O RH da instituição irá consultar seu CPF, definir sua função e ativar seu acesso.',
        isSuccess: true,
      });
    } catch (err) {
      setModal({ show: true, message: extractApiMessage(err, 'Erro ao processar requisição.'), isSuccess: false });
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    if (modal.isSuccess) navigate('/areapublica/login');
    setModal({ ...modal, show: false });
  };

  return (
    <div className="login-container">
      <h2>Pré-cadastro colaborador</h2>
      <form onSubmit={(e) => e.preventDefault()}>
        <p style={{ fontSize: '0.85rem', color: '#64748b' }}>
          Informe seus dados pessoais. A função na instituição será definida pelo RH/Administrador na ativação.
          Cadastro de nova instituição na plataforma é exclusivo do administrador master.
        </p>

        <input placeholder="Nome completo" value={nome} onChange={(e) => setNome(e.target.value)} />
        <input placeholder="CPF" value={login} onChange={(e) => setLogin(maskCPF(e.target.value))} />
        <input placeholder="RG" value={rg} onChange={(e) => setRg(e.target.value)} />
        <input type="date" value={dataDeNascimento} onChange={(e) => setNascimento(e.target.value)} />
        <input type="password" placeholder="Senha de acesso (definida por você)" value={password} onChange={(e) => setPassword(e.target.value)} />
        <input placeholder="Endereço" value={endereco} onChange={(e) => setEndereco(e.target.value)} />
        <input placeholder="Telefone" type="tel" value={telefone} onChange={(e) => setTelefone(maskPhone(e.target.value))} />

        <button type="button" onClick={handleCadastro} disabled={!isFormValid || loading}>
          {loading ? 'Enviando...' : 'Enviar pré-cadastro'}
        </button>
      </form>
      <Link to="/areapublica/login">Voltar ao login</Link>

      {modal.show && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ color: modal.isSuccess ? '#2e7d32' : '#d32f2f' }}>
              {modal.isSuccess ? 'Sucesso!' : 'Erro'}
            </h3>
            <p>{modal.message}</p>
            <button type="button" onClick={closeModal}>Fechar</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cadastro;

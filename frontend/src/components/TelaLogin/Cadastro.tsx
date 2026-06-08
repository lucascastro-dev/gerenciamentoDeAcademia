import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PasswordInput from '../common/PasswordInput';
import PasswordStrengthHints from '../common/PasswordStrengthHints';
import '../common/PasswordFields.css';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, serializarEndereco } from '../../utils/endereco';
import { isEmailValido } from '../../utils/emailPolicy';
import { isSenhaForte } from '../../utils/passwordPolicy';
import './Login.css';

const Cadastro: React.FC = () => {
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setDataDeNascimento] = useState('');
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [senha, setSenha] = useState('');
  const [senhaConfirmacao, setSenhaConfirmacao] = useState('');

  const [modal, setModal] = useState<{ open: boolean; message: string; success: boolean }>({
    open: false,
    message: '',
    success: false,
  });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const maskCPF = (v: string) =>
    v.replace(/\D/g, '').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2').slice(0, 14);

  const maskPhone = (v: string) =>
    v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d)(\d{4})$/, '$1-$2').slice(0, 15);

  const cpfLimpo = cpf.replace(/\D/g, '');
  const telefoneLimpo = telefone.replace(/\D/g, '');
  const cepLimpo = endereco.cep.replace(/\D/g, '');
  const senhasConferem = senha === senhaConfirmacao && senhaConfirmacao.length > 0;
  const enderecoValido =
    cepLimpo.length === 8 &&
    endereco.logradouro.trim() !== '' &&
    endereco.numero.trim() !== '' &&
    endereco.cidade.trim() !== '' &&
    endereco.uf.trim().length === 2;

  const emailValido = isEmailValido(email);

  const isFormValid =
    nome.trim() !== '' &&
    cpfLimpo.length === 11 &&
    rg.trim() !== '' &&
    dataDeNascimento !== '' &&
    telefoneLimpo.length >= 10 &&
    emailValido &&
    enderecoValido &&
    isSenhaForte(senha) &&
    senhasConferem;

  const handleCadastro = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isFormValid) return;

    if (!senhasConferem) {
      setModal({ open: true, message: 'A confirmação da senha não confere.', success: false });
      return;
    }

    setLoading(true);
    try {
      await HttpService.preCadastroColaborador({
        nome: nome.trim(),
        cpf: cpfLimpo,
        rg: rg.trim(),
        dataDeNascimento,
        endereco: serializarEndereco(endereco),
        telefone: telefoneLimpo,
        email: email.trim(),
        senha,
      });
      setModal({
        open: true,
        message:
          'Pré-cadastro enviado com sucesso. O RH da sua instituição irá ativar seu acesso e definir sua função. Você poderá entrar após a ativação.',
        success: true,
      });
    } catch (err) {
      setModal({ open: true, message: extractApiMessage(err, 'Erro ao processar requisição.'), success: false });
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    if (modal.success) navigate('/areapublica/login');
    setModal((m) => ({ ...m, open: false }));
  };

  return (
    <div className="auth-page">
      <div className="auth-page__brand">EduGestão Inteligente</div>

      <div className="login-container login-container--cadastro">
        <h2>Pré-cadastro colaborador</h2>
        <p className="login-intro">
          Preencha seus dados pessoais. A função na instituição será definida pelo RH na ativação do cadastro.
          Cadastro de novas instituições na plataforma é exclusivo do administrador master.
        </p>

        <form onSubmit={handleCadastro}>
          <fieldset className="auth-form-section">
            <legend>Identificação</legend>
            <label className="login-label" htmlFor="cad-nome">Nome completo</label>
            <input
              id="cad-nome"
              placeholder="Como consta em documentos"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              autoComplete="name"
            />

            <label className="login-label" htmlFor="cad-cpf">CPF</label>
            <input
              id="cad-cpf"
              placeholder="000.000.000-00"
              inputMode="numeric"
              value={cpf}
              onChange={(e) => setCpf(maskCPF(e.target.value))}
              autoComplete="username"
            />

            <label className="login-label" htmlFor="cad-rg">RG</label>
            <input
              id="cad-rg"
              placeholder="Número do RG"
              value={rg}
              onChange={(e) => setRg(e.target.value)}
            />

            <label className="login-label" htmlFor="cad-nasc">Data de nascimento</label>
            <input
              id="cad-nasc"
              type="date"
              value={dataDeNascimento}
              onChange={(e) => setDataDeNascimento(e.target.value)}
            />
          </fieldset>

          <fieldset className="auth-form-section">
            <legend>Contato</legend>
            <label className="login-label" htmlFor="cad-tel">Telefone / WhatsApp</label>
            <input
              id="cad-tel"
              type="tel"
              placeholder="(00) 00000-0000"
              value={telefone}
              onChange={(e) => setTelefone(maskPhone(e.target.value))}
              autoComplete="tel"
            />

            <label className="login-label" htmlFor="cad-email">E-mail</label>
            <input
              id="cad-email"
              type="email"
              placeholder="nome@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
            />
            {email.trim().length > 0 && !emailValido && (
              <p className="password-field--mismatch" role="alert">
                Informe um e-mail válido (ex.: nome@dominio.com).
              </p>
            )}
          </fieldset>

          <fieldset className="auth-form-section">
            <legend>Endereço</legend>
            <p className="login-hint" style={{ marginTop: 0 }}>
              Informe o CEP para preencher logradouro, bairro e cidade. Depois informe apenas número e complemento.
            </p>
            <EnderecoFields value={endereco} onChange={setEndereco} />
          </fieldset>

          <fieldset className="auth-form-section">
            <legend>Senha de acesso</legend>
            <PasswordInput
              id="cad-senha"
              label="Criar senha"
              value={senha}
              onChange={setSenha}
              placeholder="Defina uma senha forte"
              autoComplete="new-password"
              showStrength
              strengthHints={<PasswordStrengthHints password={senha} idPrefix="cad" />}
            />

            <PasswordInput
              id="cad-senha-conf"
              label="Confirmar senha"
              value={senhaConfirmacao}
              onChange={setSenhaConfirmacao}
              placeholder="Repita a senha"
              autoComplete="new-password"
            />
            {senhaConfirmacao.length > 0 && !senhasConferem && (
              <p className="password-field--mismatch" role="alert">
                As senhas não coincidem.
              </p>
            )}
          </fieldset>

          <button type="submit" className="auth-btn-primary" disabled={!isFormValid || loading}>
            {loading ? 'Enviando...' : 'Enviar pré-cadastro'}
          </button>
        </form>

        <div className="links-container">
          <Link to="/areapublica/login">Já tenho cadastro — voltar ao login</Link>
        </div>
      </div>

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        title={modal.success ? 'Pré-cadastro enviado' : 'Não foi possível enviar'}
        message={modal.message}
        onClose={closeModal}
      />
    </div>
  );
};

export default Cadastro;

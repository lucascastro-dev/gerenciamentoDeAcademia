import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PasswordInput from '../common/PasswordInput';
import PasswordStrengthHints from '../common/PasswordStrengthHints';
import PhoneInput from '../common/PhoneInput';
import AuthLayout from '../common/AuthLayout';
import { COPY, PLACEHOLDERS } from '../../constants/copy';
import '../common/AuthLayout.css';
import '../common/PasswordFields.css';
import '../common/PhoneFields.css';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, serializarEndereco } from '../../utils/endereco';
import { isEmailValido } from '../../utils/emailPolicy';
import { isSenhaForte } from '../../utils/passwordPolicy';
import { telefoneParaApi, telefoneValido } from '../../utils/phoneFormat';

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

  const cpfLimpo = cpf.replace(/\D/g, '');
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
    telefoneValido(telefone) &&
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
        telefone: telefoneParaApi(telefone),
        email: email.trim(),
        senha,
      });
      setModal({
        open: true,
        message: COPY.cadastroSucesso,
        success: true,
      });
    } catch (err) {
      setModal({ open: true, message: extractApiMessage(err, 'Não foi possível enviar o pré-cadastro. Tente novamente.'), success: false });
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    if (modal.success) navigate('/entrar');
    setModal((m) => ({ ...m, open: false }));
  };

  return (
    <AuthLayout
      wide
      title="Pré-cadastro de colaborador"
      subtitle={COPY.cadastroIntro}
    >
      <p className="auth-hint--info">
        {COPY.cadastroNotaInstituicao}{' '}
        <Link to="/contato">Fale conosco</Link>
      </p>

      <form onSubmit={handleCadastro}>
        <fieldset className="auth-form-section">
          <legend>Identificação</legend>
          <label className="auth-label" htmlFor="cad-nome">Nome completo</label>
          <input
            id="cad-nome"
            placeholder={PLACEHOLDERS.nomeCompleto}
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            autoComplete="name"
          />

          <div className="auth-form-row">
            <div>
              <label className="auth-label" htmlFor="cad-cpf">CPF</label>
              <input
                id="cad-cpf"
                placeholder={PLACEHOLDERS.cpf}
                inputMode="numeric"
                value={cpf}
                onChange={(e) => setCpf(maskCPF(e.target.value))}
                autoComplete="username"
              />
            </div>
            <div>
              <label className="auth-label" htmlFor="cad-rg">RG</label>
              <input
                id="cad-rg"
                placeholder={PLACEHOLDERS.rg}
                value={rg}
                onChange={(e) => setRg(e.target.value)}
              />
            </div>
          </div>

          <label className="auth-label" htmlFor="cad-nasc">Data de nascimento</label>
          <input
            id="cad-nasc"
            type="date"
            value={dataDeNascimento}
            onChange={(e) => setDataDeNascimento(e.target.value)}
          />
        </fieldset>

        <fieldset className="auth-form-section">
          <legend>Contato</legend>
          <PhoneInput
            id="cad-tel"
            label="Telefone / WhatsApp"
            labelClassName="auth-label"
            value={telefone}
            onChange={setTelefone}
            required
          />

          <label className="auth-label" htmlFor="cad-email">E-mail</label>
          <input
            id="cad-email"
            type="email"
            placeholder={PLACEHOLDERS.email}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
          {email.trim().length > 0 && !emailValido && (
            <p className="password-field--mismatch" role="alert">
              Informe um e-mail válido.
            </p>
          )}
        </fieldset>

        <fieldset className="auth-form-section">
          <legend>Endereço</legend>
          <p className="auth-hint" style={{ marginTop: 0 }}>
            Informe o CEP para preencher logradouro e cidade automaticamente.
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
            placeholder={PLACEHOLDERS.senhaNova}
            autoComplete="new-password"
            showStrength
            strengthHints={<PasswordStrengthHints password={senha} idPrefix="cad" />}
          />

          <PasswordInput
            id="cad-senha-conf"
            label="Confirmar senha"
            value={senhaConfirmacao}
            onChange={setSenhaConfirmacao}
            placeholder={PLACEHOLDERS.senhaConfirmacao}
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

      <div className="auth-links">
        <Link to="/entrar">Já tenho cadastro — entrar</Link>
      </div>

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        title={modal.success ? 'Pré-cadastro enviado' : 'Não foi possível enviar'}
        message={modal.message}
        onClose={closeModal}
      />
    </AuthLayout>
  );
};

export default Cadastro;

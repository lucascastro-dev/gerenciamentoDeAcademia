import { useEffect, useState } from 'react';
import EnderecoFields from '../components/common/EnderecoFields';
import FeedbackModal from '../components/common/FeedbackModal';
import PageShell from '../components/common/PageShell';
import PasswordInput from '../components/common/PasswordInput';
import PasswordStrengthHints from '../components/common/PasswordStrengthHints';
import '../components/common/PasswordFields.css';
import HttpService from '../services/HttpService';
import { extractApiMessage } from '../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../utils/endereco';
import { isSenhaForte } from '../utils/passwordPolicy';

const MeuPerfil: React.FC = () => {
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [cpf, setCpf] = useState('');
  const [dataDeNascimento, setDataDeNascimento] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [especializacao, setEspecializacao] = useState('');
  const [tipo, setTipo] = useState('');
  const [senhaAtual, setSenhaAtual] = useState('');
  const [senhaNova, setSenhaNova] = useState('');
  const [senhaConfirma, setSenhaConfirma] = useState('');
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const senhasConferem = senhaNova === senhaConfirma;
  const podeAlterarSenha =
    senhaAtual.length > 0 &&
    isSenhaForte(senhaNova) &&
    senhaConfirma.length > 0 &&
    senhasConferem;

  useEffect(() => {
    HttpService.meuPerfil()
      .then((r) => {
        const d = r.data;
        setNome(d.nome || '');
        setRg(d.rg || '');
        setCpf(d.cpf || '');
        setDataDeNascimento(d.dataDeNascimento || '');
        setEndereco(parseEndereco(d.endereco));
        setTelefone(d.telefone || '');
        setEmail(d.email || '');
        setEspecializacao(d.especializacao || '');
        setTipo(d.tipoFuncionario || d.cargo || '');
      })
      .catch(() =>
        setModal({ open: true, success: false, message: 'Não foi possível carregar seu cadastro.' }),
      );
  }, []);

  const showOk = (message: string) => setModal({ open: true, success: true, message });
  const showErr = (message: string) => setModal({ open: true, success: false, message });

  const salvarDados = async () => {
    try {
      await HttpService.atualizarMeuPerfil({
        nome, rg, cpf, dataDeNascimento, endereco: serializarEndereco(endereco), telefone, email, especializacao,
      });
      showOk('Dados atualizados com sucesso.');
    } catch (e) {
      showErr(extractApiMessage(e, 'Erro ao salvar dados.'));
    }
  };

  const salvarSenha = async () => {
    if (!senhasConferem) {
      showErr('A confirmação da nova senha não confere.');
      return;
    }
    if (!isSenhaForte(senhaNova)) {
      showErr('A nova senha não atende aos critérios de segurança.');
      return;
    }
    try {
      await HttpService.alterarSenha({ senhaAtual, senhaNova });
      setSenhaAtual('');
      setSenhaNova('');
      setSenhaConfirma('');
      showOk('Senha alterada com sucesso.');
    } catch (e) {
      showErr(extractApiMessage(e, 'Erro ao alterar senha.'));
    }
  };

  return (
    <PageShell title="Meu cadastro" subtitle="Atualize seus dados pessoais e senha de acesso">
      <div className="card" style={{ marginBottom: '1rem' }}>
        <h3 style={{ marginTop: 0 }}>Dados pessoais</h3>
        <p style={{ color: 'var(--color-muted)', fontSize: '0.9rem' }}>Perfil: {tipo}</p>
        <div className="form-grid">
          <div><label>CPF</label><input value={cpf} disabled /></div>
          <div><label>Nome</label><input value={nome} onChange={(e) => setNome(e.target.value)} /></div>
          <div><label>RG</label><input value={rg} onChange={(e) => setRg(e.target.value)} /></div>
          <div><label>Nascimento</label><input type="date" value={dataDeNascimento} onChange={(e) => setDataDeNascimento(e.target.value)} /></div>
          <div><label>Telefone</label><input value={telefone} onChange={(e) => setTelefone(e.target.value)} /></div>
          <div>
            <label>E-mail</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nome@email.com" />
          </div>
          {tipo === 'PROFESSOR' && (
            <div><label>Especialização</label><input value={especializacao} onChange={(e) => setEspecializacao(e.target.value)} /></div>
          )}
        </div>
        <EnderecoFields value={endereco} onChange={setEndereco} />
        <div className="form-actions">
          <button type="button" className="btn-primary" onClick={salvarDados}>Salvar dados</button>
        </div>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Alterar senha</h3>
        <div className="perfil-senha-form">
          <PasswordInput
            id="perfil-senha-atual"
            label="Senha atual"
            value={senhaAtual}
            onChange={setSenhaAtual}
            placeholder="Digite a senha atual"
            autoComplete="current-password"
          />

          <div className="perfil-senha-form__row">
            <PasswordInput
              id="perfil-senha-nova"
              label="Nova senha"
              value={senhaNova}
              onChange={setSenhaNova}
              placeholder="Defina uma senha forte"
              autoComplete="new-password"
            />
            <PasswordInput
              id="perfil-senha-confirma"
              label="Confirmar nova senha"
              value={senhaConfirma}
              onChange={setSenhaConfirma}
              placeholder="Repita a nova senha"
              autoComplete="new-password"
            />
          </div>

          <PasswordStrengthHints password={senhaNova} idPrefix="perfil" />

          {senhaConfirma.length > 0 && !senhasConferem && (
            <p className="password-field--mismatch" role="alert">
              A confirmação da nova senha não confere.
            </p>
          )}
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="btn-primary"
            onClick={salvarSenha}
            disabled={!podeAlterarSenha}
          >
            Alterar senha
          </button>
        </div>
      </div>

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default MeuPerfil;

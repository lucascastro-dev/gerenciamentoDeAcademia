import { useEffect, useState } from 'react';
import EnderecoFields from '../components/common/EnderecoFields';
import FeedbackModal from '../components/common/FeedbackModal';
import PageShell from '../components/common/PageShell';
import HttpService from '../services/HttpService';
import { extractApiMessage } from '../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../utils/endereco';

const isPasswordValid = (p: string) =>
  p.length >= 8 && /[A-Z]/.test(p) && /[a-z]/.test(p) && /\d/.test(p) && /[^A-Za-z0-9]/.test(p);

const MeuPerfil: React.FC = () => {
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [cpf, setCpf] = useState('');
  const [dataDeNascimento, setDataDeNascimento] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [especializacao, setEspecializacao] = useState('');
  const [tipo, setTipo] = useState('');
  const [senhaAtual, setSenhaAtual] = useState('');
  const [senhaNova, setSenhaNova] = useState('');
  const [senhaConfirma, setSenhaConfirma] = useState('');
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

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
        nome, rg, cpf, dataDeNascimento, endereco: serializarEndereco(endereco), telefone, especializacao,
      });
      showOk('Dados atualizados com sucesso.');
    } catch (e) {
      showErr(extractApiMessage(e, 'Erro ao salvar dados.'));
    }
  };

  const salvarSenha = async () => {
    if (senhaNova !== senhaConfirma) {
      showErr('A confirmação da nova senha não confere.');
      return;
    }
    if (!isPasswordValid(senhaNova)) {
      showErr('Senha fraca: use 8+ caracteres, maiúscula, minúscula, número e especial.');
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
        <div className="form-grid">
          <div><label>Senha atual</label><input type="password" value={senhaAtual} onChange={(e) => setSenhaAtual(e.target.value)} /></div>
          <div><label>Nova senha</label><input type="password" value={senhaNova} onChange={(e) => setSenhaNova(e.target.value)} /></div>
          <div><label>Confirmar nova senha</label><input type="password" value={senhaConfirma} onChange={(e) => setSenhaConfirma(e.target.value)} /></div>
        </div>
        <div className="form-actions">
          <button type="button" className="btn-primary" onClick={salvarSenha}>Alterar senha</button>
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

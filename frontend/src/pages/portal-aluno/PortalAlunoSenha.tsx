import { useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';

const PortalAlunoSenha: React.FC = () => {
  const [senhaAtual, setSenhaAtual] = useState('');
  const [senhaNova, setSenhaNova] = useState('');
  const [senhaConfirma, setSenhaConfirma] = useState('');
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const salvar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (senhaNova !== senhaConfirma) {
      setModal({ open: true, success: false, message: 'A confirmação da nova senha não confere.' });
      return;
    }
    if (senhaNova.length < 4) {
      setModal({ open: true, success: false, message: 'A nova senha deve ter no mínimo 4 caracteres.' });
      return;
    }
    try {
      await HttpService.portalAlunoAlterarSenha({ senhaAtual, senhaNova });
      setSenhaAtual('');
      setSenhaNova('');
      setSenhaConfirma('');
      setModal({ open: true, success: true, message: 'Senha alterada com sucesso.' });
    } catch (err) {
      setModal({ open: true, success: false, message: extractApiMessage(err, 'Erro ao alterar senha.') });
    }
  };

  return (
    <PageShell title="Alterar senha" subtitle="Portal do aluno">
      <form className="card" onSubmit={salvar} style={{ maxWidth: 420 }}>
        <div className="form-grid">
          <div>
            <label>Senha atual</label>
            <input type="password" value={senhaAtual} onChange={(e) => setSenhaAtual(e.target.value)} required />
          </div>
          <div>
            <label>Nova senha</label>
            <input type="password" value={senhaNova} onChange={(e) => setSenhaNova(e.target.value)} required minLength={4} />
          </div>
          <div>
            <label>Confirmar nova senha</label>
            <input type="password" value={senhaConfirma} onChange={(e) => setSenhaConfirma(e.target.value)} required minLength={4} />
          </div>
        </div>
        <p className="field-hint">Mínimo de 4 caracteres. Após a matrícula, a senha inicial costuma ser <strong>123</strong>.</p>
        <div className="form-actions">
          <button type="submit" className="btn-primary">Salvar nova senha</button>
        </div>
      </form>
      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default PortalAlunoSenha;

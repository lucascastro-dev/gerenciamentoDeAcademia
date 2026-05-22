import { useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { INSTITUICAO } from '../../constants/branding';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';

const GestaoAcademias: React.FC = () => {
  const [cnpj, setCnpj] = useState('');
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const desativar = async () => {
    try {
      await HttpService.desativarInstituicao(cnpj.replace(/\D/g, ''));
      setModal({ open: true, success: true, message: `${INSTITUICAO.capitalized} desativada.` });
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao desativar.') });
    }
  };

  return (
    <PageShell title={`Ativar / desativar ${INSTITUICAO.singular}`}>
      <div className="card">
        <label>CNPJ</label>
        <input value={cnpj} onChange={(e) => setCnpj(e.target.value)} />
        <div className="form-actions">
          <button type="button" className="btn-danger" onClick={desativar}>
            Desativar {INSTITUICAO.singular}
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

export default GestaoAcademias;

import React, { useState } from 'react';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { INSTITUICAO } from '../../constants/branding';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';

const GerenciarAcademia: React.FC = () => {
  const [cnpj, setCnpj] = useState('');
  const [telefone, setTelefone] = useState('');
  const [razaoSocial, setRazaoSocial] = useState('');
  const [cadastroAtivo, setCadastroAtivo] = useState(false);
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [loading, setLoading] = useState(false);
  const [isEditable, setIsEditable] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const maskCNPJ = (v: string) =>
    v.toUpperCase().replace(/[^A-Z0-9]/g, '')
      .replace(/^([A-Z0-9]{2})([A-Z0-9])/, '$1.$2')
      .replace(/^([A-Z0-9]{2})\.([A-Z0-9]{3})([A-Z0-9])/, '$1.$2.$3')
      .replace(/\.([A-Z0-9]{3})([A-Z0-9])/, '.$1/$2')
      .replace(/([A-Z0-9]{4})([A-Z0-9])/, '$1-$2')
      .slice(0, 18);

  const maskPhone = (v: string) =>
    v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d)(\d{4})$/, '$1-$2').slice(0, 15);

  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  const getErrorMessage = (err: unknown) => extractApiMessage(err, 'Erro ao processar requisição.');

  const resetForm = () => {
    setRazaoSocial('');
    setTelefone('');
    setEndereco(enderecoVazio());
    setCadastroAtivo(false);
    setIsEditable(false);
  };

  const handleConsultar = async () => {
    setLoading(true);
    resetForm();
    try {
      const res = await HttpService.consultarAcademiaPorCnpj(onlyNumbers(cnpj));
      const data = res.data;
      setRazaoSocial(data.razaoSocial ?? '');
      setEndereco(parseEndereco(data.endereco));
      setTelefone(data.telefone ?? '');
      setCadastroAtivo(data.cadastroAtivo ?? false);
      setIsEditable(true);
    } catch {
      setModal({ open: true, success: false, message: 'Instituição não encontrada ou erro na busca.' });
    } finally {
      setLoading(false);
    }
  };

  const handleSalvar = async () => {
    setLoading(true);
    try {
      await HttpService.editarAcademia({
        razaoSocial,
        cnpj: onlyNumbers(cnpj),
        endereco: serializarEndereco(endereco),
        telefone: onlyNumbers(telefone),
        cadastroAtivo,
      });
      setModal({ open: true, success: true, message: 'Dados atualizados com sucesso.' });
    } catch (err) {
      setModal({ open: true, success: false, message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageShell title={`Consultar ${INSTITUICAO.plural}`}>
      <div className="card" style={{ marginBottom: '1rem' }}>
        <label>CNPJ</label>
        <input
          placeholder="Digite o CNPJ para buscar"
          value={cnpj}
          onChange={(e) => setCnpj(maskCNPJ(e.target.value))}
        />
        <div className="form-actions">
          <button type="button" className="btn-primary" onClick={handleConsultar} disabled={loading || cnpj.length < 14}>
            {loading ? 'Buscando...' : 'Consultar'}
          </button>
        </div>
      </div>

      <div className={`card ${!isEditable ? 'card--disabled' : ''}`}>
        <div className="form-grid">
          <div className="form-grid__span-2">
            <label>Razão social</label>
            <input disabled={!isEditable} value={razaoSocial} onChange={(e) => setRazaoSocial(e.target.value)} />
          </div>
          <div>
            <label>CNPJ</label>
            <input disabled value={cnpj} />
          </div>
          <div>
            <label>Telefone</label>
            <input
              disabled={!isEditable}
              type="tel"
              value={telefone}
              onChange={(e) => setTelefone(maskPhone(e.target.value))}
            />
          </div>
        </div>
        <EnderecoFields value={endereco} onChange={setEndereco} disabled={!isEditable} />
        <label className="switch-inline">
          <input
            type="checkbox"
            checked={cadastroAtivo}
            disabled={!isEditable}
            onChange={(e) => setCadastroAtivo(e.target.checked)}
          />
          Cadastro ativo
        </label>
        <div className="form-actions">
          <button type="button" className="btn-primary" onClick={handleSalvar} disabled={!isEditable || loading}>
            {loading ? 'Processando...' : 'Salvar alterações'}
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

export default GerenciarAcademia;

import React, { useState } from 'react';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';

const GerenciarFuncionario: React.FC = () => {
  const [cpf, setCpf] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setNascimento] = useState('');
  const [cargo, setCargo] = useState('');
  const [especializacao, setEspecializacao] = useState('');
  const [permitirGerenciarFuncoes, setGerenciarFuncoes] = useState(false);
  const [cadastroAtivo, setCadastroAtivo] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isEditable, setIsEditable] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const maskCPF = (v: string) =>
    v.replace(/\D/g, '').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d)/, '$1.$2').replace(/(\d{3})(\d{1,2})$/, '$1-$2').slice(0, 14);

  const maskPhone = (v: string) =>
    v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d)(\d{4})$/, '$1-$2').slice(0, 15);

  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  const getErrorMessage = (err: unknown) => extractApiMessage(err, 'Erro ao processar requisição.');

  const resetForm = () => {
    setNome('');
    setRg('');
    setNascimento('');
    setCargo('');
    setEspecializacao('');
    setEndereco(enderecoVazio());
    setTelefone('');
    setGerenciarFuncoes(false);
    setCadastroAtivo(false);
    setIsEditable(false);
  };

  const handleConsultar = async () => {
    if (!localStorage.getItem('@App:token')) {
      setModal({ open: true, success: false, message: 'Sessão expirada. Faça login novamente.' });
      return;
    }
    setLoading(true);
    resetForm();
    try {
      const res = await HttpService.consultarFuncionarioPorCpf(onlyNumbers(cpf));
      const data = res.data;
      setNome(data.nome ?? '');
      setRg(data.rg ?? '');
      setNascimento(data.dataDeNascimento ?? '');
      setCargo(data.cargo ?? data.tipoFuncionario ?? '');
      setEspecializacao(data.especializacao ?? '');
      setEndereco(parseEndereco(data.endereco));
      setTelefone(data.telefone ?? '');
      setGerenciarFuncoes(data.permitirGerenciarFuncoes ?? false);
      setCadastroAtivo(data.cadastroAtivo ?? false);
      setIsEditable(true);
    } catch {
      setModal({ open: true, success: false, message: 'Funcionário não encontrado.' });
    } finally {
      setLoading(false);
    }
  };

  const handleSalvar = async () => {
    setLoading(true);
    try {
      await HttpService.editarPessoa({
        nome,
        cpf: onlyNumbers(cpf),
        rg,
        dataDeNascimento,
        cargo,
        especializacao,
        endereco: serializarEndereco(endereco),
        telefone: onlyNumbers(telefone),
        permitirGerenciarFuncoes,
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
    <PageShell title="Funcionários">
      <div className="card" style={{ marginBottom: '1rem' }}>
        <label>CPF</label>
        <input
          placeholder="Digite o CPF para buscar"
          value={cpf}
          onChange={(e) => setCpf(maskCPF(e.target.value))}
        />
        <div className="form-actions">
          <button type="button" className="btn-primary" onClick={handleConsultar} disabled={loading || cpf.length < 14}>
            {loading ? 'Buscando...' : 'Consultar'}
          </button>
        </div>
      </div>

      <div className={`card ${!isEditable ? 'card--disabled' : ''}`}>
        <div className="form-grid">
          <div className="form-grid__span-2"><label>Nome</label><input disabled={!isEditable} value={nome} onChange={(e) => setNome(e.target.value)} /></div>
          <div><label>CPF</label><input disabled value={cpf} /></div>
          <div><label>RG</label><input disabled={!isEditable} value={rg} onChange={(e) => setRg(e.target.value)} /></div>
          <div><label>Nascimento</label><input disabled={!isEditable} type="date" value={dataDeNascimento} onChange={(e) => setNascimento(e.target.value)} /></div>
          <div><label>Cargo</label><input disabled={!isEditable} value={cargo} onChange={(e) => setCargo(e.target.value)} /></div>
          <div><label>Especialização</label><input disabled={!isEditable} value={especializacao} onChange={(e) => setEspecializacao(e.target.value)} /></div>
          <div><label>Telefone</label><input disabled={!isEditable} type="tel" value={telefone} onChange={(e) => setTelefone(maskPhone(e.target.value))} /></div>
        </div>
        <EnderecoFields value={endereco} onChange={setEndereco} disabled={!isEditable} />
        <div className="form-grid" style={{ marginTop: '0.75rem' }}>
          <label className="switch-inline">
            <input type="checkbox" disabled={!isEditable} checked={permitirGerenciarFuncoes} onChange={(e) => setGerenciarFuncoes(e.target.checked)} />
            Permite gerenciar funcionalidades
          </label>
          <label className="switch-inline">
            <input type="checkbox" disabled={!isEditable} checked={cadastroAtivo} onChange={(e) => setCadastroAtivo(e.target.checked)} />
            Cadastro ativo
          </label>
        </div>
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

export default GerenciarFuncionario;

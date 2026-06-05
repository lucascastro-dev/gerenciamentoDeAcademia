import React, { useState } from 'react';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { carregarSessao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';

const TIPOS = [
  'ADMINISTRADOR', 'TI', 'FINANCEIRO', 'RH', 'RECEPCIONISTA', 'PROFESSOR',
  'ESTAGIARIO', 'SERVICOS_GERAIS', 'TERCEIRIZADO',
] as const;

const GerenciarFuncionario: React.FC = () => {
  const [cpf, setCpf] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setNascimento] = useState('');
  const [tipoFuncionario, setTipoFuncionario] = useState<string>('RECEPCIONISTA');
  const [areaTerceirizado, setAreaTerceirizado] = useState('RH');
  const [especializacao, setEspecializacao] = useState('');
  const [permitirGerenciarFuncoes, setGerenciarFuncoes] = useState(false);
  const [cadastroAtivo, setCadastroAtivo] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isEditable, setIsEditable] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const sessao = carregarSessao();

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
    setTipoFuncionario('RECEPCIONISTA');
    setAreaTerceirizado('RH');
    setEspecializacao('');
    setEndereco(enderecoVazio());
    setTelefone('');
    setEmail('');
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
      const tipo = data.tipoFuncionario || data.cargo || 'RECEPCIONISTA';
      setTipoFuncionario(String(tipo).replace(/ /g, '_').toUpperCase());
      if (data.areaTerceirizado) setAreaTerceirizado(data.areaTerceirizado);
      setEspecializacao(data.especializacao ?? '');
      setEndereco(parseEndereco(data.endereco));
      setTelefone(data.telefone ?? '');
      setEmail(data.email ?? '');
      setGerenciarFuncoes(data.permitirGerenciarFuncoes ?? false);
      setCadastroAtivo(data.cadastroAtivo ?? false);
      setIsEditable(true);
    } catch {
      setModal({ open: true, success: false, message: 'Funcionário não encontrado.' });
    } finally {
      setLoading(false);
    }
  };

  const toggleSubMaster = async () => {
    if (!sessao?.masterRaiz) return;
    setLoading(true);
    try {
      const habilitar = !permitirGerenciarFuncoes;
      await HttpService.definirSubMaster(onlyNumbers(cpf), habilitar);
      setGerenciarFuncoes(habilitar);
      setModal({
        open: true,
        success: true,
        message: habilitar
          ? 'Colaborador habilitado como operador sub-master da plataforma.'
          : 'Delegação de sub-master removida.',
      });
    } catch (err) {
      setModal({ open: true, success: false, message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  const handleSalvar = async () => {
    if (tipoFuncionario === 'PROFESSOR' && !especializacao.trim()) {
      setModal({ open: true, success: false, message: 'Informe a especialização para professores.' });
      return;
    }
    setLoading(true);
    try {
      await HttpService.editarPessoa({
        nome,
        cpf: onlyNumbers(cpf),
        rg,
        dataDeNascimento,
        tipoFuncionario,
        areaTerceirizado: tipoFuncionario === 'TERCEIRIZADO' ? areaTerceirizado : null,
        especializacao: tipoFuncionario === 'PROFESSOR' ? especializacao : null,
        endereco: serializarEndereco(endereco),
        telefone: onlyNumbers(telefone),
        email,
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
          <div>
            <label>Cargo / perfil</label>
            <select disabled={!isEditable} value={tipoFuncionario} onChange={(e) => setTipoFuncionario(e.target.value)}>
              {TIPOS.map((t) => (
                <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>
          {tipoFuncionario === 'TERCEIRIZADO' && (
            <div>
              <label>Área do terceirizado</label>
              <select disabled={!isEditable} value={areaTerceirizado} onChange={(e) => setAreaTerceirizado(e.target.value)}>
                <option value="RH">Apoio RH</option>
                <option value="PROFESSOR_SUBSTITUTO">Professor substituto</option>
                <option value="TI">Apoio TI</option>
              </select>
            </div>
          )}
          {tipoFuncionario === 'PROFESSOR' && (
            <div>
              <label>Especialização</label>
              <input disabled={!isEditable} value={especializacao} onChange={(e) => setEspecializacao(e.target.value)} />
            </div>
          )}
          <div><label>Telefone</label><input disabled={!isEditable} type="tel" value={telefone} onChange={(e) => setTelefone(maskPhone(e.target.value))} /></div>
          <div>
            <label>E-mail</label>
            <input disabled={!isEditable} type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nome@email.com" />
          </div>
        </div>
        <EnderecoFields value={endereco} onChange={setEndereco} disabled={!isEditable} />
        <div className="form-grid" style={{ marginTop: '0.75rem' }}>
          <label className="switch-inline">
            <input type="checkbox" disabled={!isEditable} checked={cadastroAtivo} onChange={(e) => setCadastroAtivo(e.target.checked)} />
            Cadastro ativo
          </label>
          {permitirGerenciarFuncoes && (
            <p className="field-hint">Operador sub-master da plataforma (não pode criar outros masters).</p>
          )}
        </div>
        <div className="form-actions">
          {sessao?.masterRaiz && isEditable && (
            <button
              type="button"
              className="btn-secondary"
              onClick={toggleSubMaster}
              disabled={loading}
            >
              {permitirGerenciarFuncoes ? 'Revogar sub-master' : 'Habilitar sub-master'}
            </button>
          )}
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

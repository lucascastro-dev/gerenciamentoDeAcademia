import { useEffect, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { carregarSessao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';

const TIPOS = [
  'ADMINISTRADOR', 'TI', 'FINANCEIRO', 'RH', 'RECEPCIONISTA', 'PROFESSOR',
  'ESTAGIARIO', 'SERVICOS_GERAIS', 'TERCEIRIZADO',
] as const;

const GestaoCadastros: React.FC = () => {
  const sessao = carregarSessao();
  const instituicaoId = sessao?.vinculo || '1';
  const [nomeInstituicao, setNomeInstituicao] = useState('');
  const [cpf, setCpf] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [cadastroAtivo, setCadastroAtivo] = useState<boolean | null>(null);
  const [tipoFuncionario, setTipoFuncionario] = useState('RECEPCIONISTA');
  const [areaTerceirizado, setAreaTerceirizado] = useState('RH');
  const [especializacao, setEspecializacao] = useState('');
  const [carregando, setCarregando] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  useEffect(() => {
    HttpService.consultarInstituicao(instituicaoId)
      .then((r) => setNomeInstituicao(r.data.razaoSocial || 'Instituição logada'))
      .catch(() => setNomeInstituicao('Instituição logada'));
  }, [instituicaoId]);

  const consultar = async () => {
    const cpfLimpo = onlyNumbers(cpf);
    if (cpfLimpo.length < 11) {
      setModal({ open: true, success: false, message: 'Informe um CPF válido.' });
      return;
    }
    setCarregando(true);
    try {
      const r = await HttpService.consultarFuncionarioPorCpf(cpfLimpo);
      const d = r.data;
      setNome(d.nome || '');
      setRg(d.rg || '');
      setCadastroAtivo(!!d.cadastroAtivo);
      if (d.tipoFuncionario) setTipoFuncionario(d.tipoFuncionario);
      if (d.areaTerceirizado) setAreaTerceirizado(d.areaTerceirizado);
      if (d.especializacao) setEspecializacao(d.especializacao);
    } catch (e) {
      setNome('');
      setRg('');
      setCadastroAtivo(null);
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Colaborador não encontrado.') });
    } finally {
      setCarregando(false);
    }
  };

  const ativar = async () => {
    try {
      await HttpService.ativarFuncionarioInstituicao(instituicaoId, onlyNumbers(cpf), {
        tipoFuncionario,
        areaTerceirizado: tipoFuncionario === 'TERCEIRIZADO' ? areaTerceirizado : null,
        especializacao: tipoFuncionario === 'PROFESSOR' ? especializacao : null,
      });
      setModal({
        open: true,
        success: true,
        message: 'Colaborador vinculado à instituição e ativado com sucesso.',
      });
      setCadastroAtivo(true);
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao ativar.') });
    }
  };

  const inativar = async () => {
    try {
      await HttpService.inativarFuncionarioInstituicao(instituicaoId, onlyNumbers(cpf));
      setModal({ open: true, success: true, message: 'Colaborador inativado nesta instituição.' });
      setCadastroAtivo(false);
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao inativar.') });
    }
  };

  return (
    <PageShell
      title="Ativação de cadastros"
      subtitle="Vincule colaboradores à instituição em que você está logado"
    >
      <div className="card" style={{ marginBottom: '1rem' }}>
        <p className="field-hint" style={{ marginTop: 0 }}>
          Instituição atual: <strong>{nomeInstituicao}</strong> (não é necessário informar CNPJ)
        </p>
        <div className="form-grid">
          <div>
            <label>CPF do colaborador</label>
            <input value={cpf} onChange={(e) => setCpf(e.target.value)} placeholder="000.000.000-00" />
          </div>
        </div>
        <div className="form-actions form-actions--compact">
          <button type="button" className="btn-primary" onClick={consultar} disabled={carregando}>
            {carregando ? 'Buscando...' : 'Consultar CPF'}
          </button>
        </div>
      </div>

      {nome && (
        <div className="card">
          <h3 style={{ marginTop: 0 }}>{nome}</h3>
          <p className="field-hint">RG: {rg} · Situação: {cadastroAtivo ? 'Ativo' : 'Aguardando ativação'}</p>

          <h4>Função nesta instituição</h4>
          <div className="form-grid">
            <div>
              <label>Perfil / cargo</label>
              <select value={tipoFuncionario} onChange={(e) => setTipoFuncionario(e.target.value)}>
                {TIPOS.map((t) => (
                  <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </div>
            {tipoFuncionario === 'TERCEIRIZADO' && (
              <div>
                <label>Área do terceirizado</label>
                <select value={areaTerceirizado} onChange={(e) => setAreaTerceirizado(e.target.value)}>
                  <option value="RH">Apoio RH</option>
                  <option value="PROFESSOR_SUBSTITUTO">Professor substituto</option>
                  <option value="TI">Apoio TI</option>
                </select>
              </div>
            )}
            {tipoFuncionario === 'PROFESSOR' && (
              <div>
                <label>Especialização</label>
                <input value={especializacao} onChange={(e) => setEspecializacao(e.target.value)} />
              </div>
            )}
          </div>

          <div className="form-actions">
            <button type="button" className="btn-primary" onClick={ativar}>
              Vincular e ativar
            </button>
            <button type="button" className="btn-danger" onClick={inativar}>
              Inativar
            </button>
          </div>
        </div>
      )}

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default GestaoCadastros;

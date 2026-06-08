import { useEffect, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { carregarSessao, isModoPlataforma } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { mapInstituicoesApi } from '../../utils/instituicao';

const TIPOS = [
  'ADMINISTRADOR', 'TI', 'FINANCEIRO', 'RH', 'RECEPCIONISTA', 'PROFESSOR',
  'ESTAGIARIO', 'SERVICOS_GERAIS', 'TERCEIRIZADO',
] as const;

interface InstituicaoOpt {
  id: number;
  razaoSocial: string;
  cadastroAtivo?: boolean;
}

const GestaoCadastros: React.FC = () => {
  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);
  const instituicaoIdPadrao = sessao?.vinculo && sessao.vinculo !== '0' ? sessao.vinculo : '';
  const [instituicaoId, setInstituicaoId] = useState<string>(instituicaoIdPadrao);
  const [instituicoes, setInstituicoes] = useState<InstituicaoOpt[]>([]);
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

  const maskCPF = (v: string) =>
    v.replace(/\D/g, '')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
      .slice(0, 14);

  useEffect(() => {
    if (master) {
      HttpService.listarTodasInstituicoes()
        .then((r) => {
          const ativas = mapInstituicoesApi((r.data || []).filter((i: InstituicaoOpt) => i.cadastroAtivo));
          setInstituicoes(ativas);
        })
        .catch(() => setInstituicoes([]));
    } else if (instituicaoIdPadrao) {
      HttpService.consultarInstituicao(instituicaoIdPadrao)
        .then((r) => setNomeInstituicao(r.data.razaoSocial || 'Instituição logada'))
        .catch(() => setNomeInstituicao('Instituição logada'));
    }
  }, [master, instituicaoIdPadrao]);

  useEffect(() => {
    if (!master && instituicaoIdPadrao) {
      setInstituicaoId(instituicaoIdPadrao);
    }
  }, [master, instituicaoIdPadrao]);

  const consultar = async () => {
    const cpfLimpo = onlyNumbers(cpf);
    if (cpfLimpo.length < 11) {
      setModal({ open: true, success: false, message: 'Informe um CPF válido.' });
      return;
    }
    if (master && !instituicaoId) {
      setModal({ open: true, success: false, message: 'Selecione a instituição de destino.' });
      return;
    }
    setCarregando(true);
    try {
      const r = await HttpService.consultarFuncionarioPorCpf(cpfLimpo);
      const d = r.data;
      setNome(d.nome || '');
      setRg(d.rg || '');
      setCadastroAtivo(!!d.cadastroAtivo);
      const idAlvo = master ? instituicaoId : instituicaoIdPadrao;
      const vinculo = d.vinculos?.find((v) => String(v.instituicaoId) === String(idAlvo));
      if (vinculo?.tipoFuncionario) setTipoFuncionario(vinculo.tipoFuncionario);
      if (vinculo?.areaTerceirizado) setAreaTerceirizado(vinculo.areaTerceirizado);
      if (vinculo?.especializacao) setEspecializacao(vinculo.especializacao);
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
    const id = master ? instituicaoId : instituicaoIdPadrao;
    if (!id) {
      setModal({ open: true, success: false, message: 'Instituição não definida.' });
      return;
    }
    try {
      await HttpService.ativarFuncionarioInstituicao(id, onlyNumbers(cpf), {
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
    const id = master ? instituicaoId : instituicaoIdPadrao;
    try {
      await HttpService.inativarFuncionarioInstituicao(id, onlyNumbers(cpf));
      setModal({ open: true, success: true, message: 'Colaborador inativado nesta instituição.' });
      setCadastroAtivo(false);
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao inativar.') });
    }
  };

  return (
    <PageShell
      title="Ativação de cadastros"
      subtitle={
        master
          ? 'Consulte o colaborador pelo CPF e vincule à instituição ativa escolhida'
          : 'Vincule colaboradores à instituição em que você está logado'
      }
    >
      <div className="card" style={{ marginBottom: '1rem' }}>
        {master ? (
          <div className="form-grid">
            <div className="form-grid__span-2">
              <label>Instituição de destino (somente ativas)</label>
              <select value={instituicaoId} onChange={(e) => setInstituicaoId(e.target.value)}>
                <option value="">Selecione a instituição</option>
                {instituicoes.map((i) => (
                  <option key={i.id} value={String(i.id)}>{i.razaoSocial}</option>
                ))}
              </select>
            </div>
          </div>
        ) : (
          <p className="field-hint" style={{ marginTop: 0 }}>
            Instituição atual: <strong>{nomeInstituicao}</strong>
          </p>
        )}
        <div className="form-grid" style={{ marginTop: '1rem' }}>
          <div>
            <label>CPF do colaborador</label>
            <input
              value={cpf}
              onChange={(e) => setCpf(maskCPF(e.target.value))}
              placeholder="000.000.000-00"
            />
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

          <h4>Função na instituição</h4>
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

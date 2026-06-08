import React, { useCallback, useEffect, useState } from 'react';

import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import ListaConsultaInstituicoes, { InstituicaoListagemItem } from '../common/ListaConsultaInstituicoes';
import PageShell from '../common/PageShell';

import { INSTITUICAO } from '../../constants/branding';

import HttpService from '../../services/HttpService';

import { extractApiMessage } from '../../utils/apiError';
import { filtrarTiposPlano } from '../../utils/planoInstituicao';
import { formatDateBr } from '../../utils/format';

import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';

const STATUS_FINANCEIRO = [
  { value: 'NAO_APLICAVEL', label: 'Não aplicável' },
  { value: 'PENDENTE_PAGAMENTO', label: 'Pagamento pendente' },
  { value: 'PAGAMENTO_CONFIRMADO', label: 'Pagamento confirmado' },
];

const GerenciarAcademia: React.FC = () => {
  const [tela, setTela] = useState<'lista' | 'detalhe'>('lista');
  const [lista, setLista] = useState<InstituicaoListagemItem[]>([]);
  const [carregandoLista, setCarregandoLista] = useState(true);

  const [cnpj, setCnpj] = useState('');
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [razaoSocial, setRazaoSocial] = useState('');
  const [cadastroAtivo, setCadastroAtivo] = useState(false);
  const [statusFinanceiro, setStatusFinanceiro] = useState('NAO_APLICAVEL');
  const [trialUtilizado, setTrialUtilizado] = useState(false);
  const [plano, setPlano] = useState('');
  const [planoVigente, setPlanoVigente] = useState(false);
  const [planoDataFim, setPlanoDataFim] = useState('');
  const [tiposPlano, setTiposPlano] = useState<Array<{ codigo: string; descricao: string }>>([]);
  const [cpfAdministrador, setCpfAdministrador] = useState('');
  const [nomeAdministrador, setNomeAdministrador] = useState('');
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

  const maskCPF = (v: string) =>
    v.replace(/\D/g, '')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
      .slice(0, 14);

  const maskPhone = (v: string) =>
    v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d)(\d{4})$/, '$1-$2').slice(0, 15);

  const onlyNumbers = (v: string) => v.replace(/\D/g, '');
  const onlyAlnumCnpj = (v: string) => v.replace(/[^A-Za-z0-9]/g, '').toUpperCase();

  const getErrorMessage = (err: unknown) => extractApiMessage(err, 'Erro ao processar requisição.');

  const carregarLista = useCallback(() => {
    setCarregandoLista(true);
    HttpService.listarInstituicoesResumo()
      .then((r) => setLista(r.data || []))
      .catch(() => setLista([]))
      .finally(() => setCarregandoLista(false));
  }, []);

  useEffect(() => {
    carregarLista();
  }, [carregarLista]);

  useEffect(() => {
    HttpService.tiposPlanoInstituicao()
      .then((r) => setTiposPlano(filtrarTiposPlano(r.data, trialUtilizado)))
      .catch(() => setTiposPlano([]));
  }, [trialUtilizado]);

  const resetForm = () => {
    setCnpj('');
    setRazaoSocial('');
    setTelefone('');
    setEmail('');
    setEndereco(enderecoVazio());
    setCadastroAtivo(false);
    setStatusFinanceiro('NAO_APLICAVEL');
    setTrialUtilizado(false);
    setPlano('');
    setPlanoVigente(false);
    setPlanoDataFim('');
    setCpfAdministrador('');
    setNomeAdministrador('');
    setIsEditable(false);
  };

  const voltarLista = () => {
    resetForm();
    setTela('lista');
  };

  const aplicarDetalhe = (data: {
    razaoSocial?: string;
    endereco?: string;
    telefone?: string;
    email?: string;
    cadastroAtivo?: boolean;
    statusFinanceiro?: string;
    trialUtilizado?: boolean;
    cpfAdministrador?: string;
    nomeAdministrador?: string;
    assinatura?: { plano?: string; dataFim?: string; vigente?: boolean };
  }) => {
    setRazaoSocial(data.razaoSocial ?? '');
    setEndereco(parseEndereco(data.endereco));
    setTelefone(data.telefone ?? '');
    setEmail(data.email ?? '');
    setCadastroAtivo(!!data.cadastroAtivo);
    setStatusFinanceiro(data.statusFinanceiro ?? 'NAO_APLICAVEL');
    setTrialUtilizado(data.trialUtilizado ?? false);
    const cpfAdmin = data.cpfAdministrador ?? '';
    setCpfAdministrador(cpfAdmin ? maskCPF(cpfAdmin) : '');
    setNomeAdministrador(data.nomeAdministrador ?? '');
    setPlano(data.assinatura?.plano || '');
    setPlanoVigente(!!data.assinatura?.vigente);
    setPlanoDataFim(data.assinatura?.dataFim || '');
    setIsEditable(true);
  };

  const recarregarDetalhe = async () => {
    const res = await HttpService.consultarInstituicaoDetalheCnpj(onlyAlnumCnpj(cnpj));
    aplicarDetalhe(res.data);
  };

  const abrirDetalhe = async (item: InstituicaoListagemItem) => {
    resetForm();
    const cnpjLimpo = item.cnpj || onlyAlnumCnpj(item.cnpjExibicao || '');
    if (cnpjLimpo.length < 14) {
      setModal({ open: true, success: false, message: 'Não foi possível identificar o CNPJ da instituição.' });
      return;
    }
    setCnpj(maskCNPJ(cnpjLimpo));
    setTela('detalhe');
    setLoading(true);
    try {
      await recarregarDetalhe();
    } catch {
      setModal({ open: true, success: false, message: 'Instituição não encontrada ou erro na busca.' });
      setTela('lista');
    } finally {
      setLoading(false);
    }
  };

  const handleSalvar = async () => {
    setLoading(true);
    try {
      await HttpService.editarInstituicao({
        razaoSocial,
        cnpj: onlyAlnumCnpj(cnpj),
        endereco: serializarEndereco(endereco),
        telefone: onlyNumbers(telefone),
        email,
        cadastroAtivo,
      });
      await HttpService.atualizarStatusFinanceiro({
        cnpj: onlyAlnumCnpj(cnpj),
        statusFinanceiro,
      });
      if (cadastroAtivo && plano) {
        await HttpService.atualizarPlanoInstituicao({ cnpj: onlyAlnumCnpj(cnpj), plano });
      }
      setModal({ open: true, success: true, message: 'Dados atualizados com sucesso.' });
      await recarregarDetalhe();
      carregarLista();
    } catch (err) {
      setModal({ open: true, success: false, message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  const handleAtivarCadastro = async () => {
    if (!plano) {
      setModal({ open: true, success: false, message: 'Selecione o plano antes de ativar o cadastro.' });
      return;
    }
    setLoading(true);
    try {
      const res = await HttpService.ativarCadastroInstituicao(onlyAlnumCnpj(cnpj), plano);
      aplicarDetalhe(res.data);
      setModal({ open: true, success: true, message: 'Cadastro ativado e plano registrado. Usuários já podem entrar se o plano estiver vigente.' });
      carregarLista();
    } catch (err) {
      setModal({ open: true, success: false, message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  const handleDesativarCadastro = async () => {
    if (!window.confirm('Desativar o cadastro? Nenhum usuário poderá entrar nesta instituição.')) return;
    setLoading(true);
    try {
      await HttpService.desativarInstituicao(onlyAlnumCnpj(cnpj));
      setCadastroAtivo(false);
      setModal({ open: true, success: true, message: 'Cadastro da instituição desativado.' });
      await recarregarDetalhe();
      carregarLista();
    } catch (err) {
      setModal({ open: true, success: false, message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  const handleSalvarAdministrador = async () => {
    const cpf = onlyNumbers(cpfAdministrador);
    if (cpf.length < 11) {
      setModal({ open: true, success: false, message: 'Informe o CPF do administrador com 11 dígitos.' });
      return;
    }
    setLoading(true);
    try {
      const res = await HttpService.trocarAdministradorInstituicao({
        cnpj: onlyAlnumCnpj(cnpj),
        cpfAdministrador: cpf,
      });
      aplicarDetalhe(res.data);
      setModal({ open: true, success: true, message: 'Administrador atualizado na instituição.' });
    } catch (err) {
      setModal({ open: true, success: false, message: getErrorMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageShell
      title={`Consultar ${INSTITUICAO.plural}`}
      subtitle="Listagem de todas as instituições da base — busca e paginação na tabela"
    >
      {tela === 'lista' && (
        <div className="card">
          <ListaConsultaInstituicoes
            itens={lista}
            carregando={carregandoLista}
            onVerDetalhes={abrirDetalhe}
          />
        </div>
      )}

      {tela === 'detalhe' && isEditable && (
        <>
          <div className="form-actions" style={{ marginBottom: '1rem' }}>
            <button type="button" className="btn-secondary" onClick={voltarLista}>
              ← Voltar à lista
            </button>
          </div>

          <div className="card">
            <div className="form-grid">
              <div className="form-grid__span-2">
                <label>Razão social</label>
                <input value={razaoSocial} onChange={(e) => setRazaoSocial(e.target.value)} />
              </div>
              <div>
                <label>CNPJ</label>
                <input disabled value={cnpj} />
              </div>
              <div>
                <label>Telefone</label>
                <input
                  type="tel"
                  value={telefone}
                  onChange={(e) => setTelefone(maskPhone(e.target.value))}
                />
              </div>
              <div>
                <label>E-mail da instituição</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="contato@instituicao.edu.br"
                />
              </div>
              <div>
                <label>Status financeiro</label>
                <select
                  value={statusFinanceiro}
                  onChange={(e) => setStatusFinanceiro(e.target.value)}
                >
                  {STATUS_FINANCEIRO.map((s) => (
                    <option key={s.value} value={s.value}>{s.label}</option>
                  ))}
                </select>
              </div>
              <div>
                <label>Plano da instituição</label>
                <select
                  value={plano}
                  onChange={(e) => setPlano(e.target.value)}
                >
                  <option value="">Selecione o plano</option>
                  {tiposPlano.map((t) => (
                    <option key={t.codigo} value={t.codigo}>{t.descricao}</option>
                  ))}
                </select>
                {!cadastroAtivo && (
                  <p className="field-hint" style={{ marginTop: '0.35rem' }}>
                    Obrigatório ao clicar em Ativar cadastro.
                  </p>
                )}
                {cadastroAtivo && (
                  <p className="field-hint" style={{ marginTop: '0.35rem' }}>
                    {planoVigente ? (
                      <>
                        Plano <strong>vigente</strong>
                        {planoDataFim ? ` até ${formatDateBr(planoDataFim)}` : ''}
                      </>
                    ) : (
                      <strong style={{ color: '#b45309' }}>
                        Sem plano vigente — usuários não conseguem entrar até salvar um plano.
                      </strong>
                    )}
                  </p>
                )}
              </div>
            </div>
            <EnderecoFields value={endereco} onChange={setEndereco} />

            <div className="card" style={{ marginTop: '1rem', padding: '1rem', background: 'var(--color-bg-soft, #f8fafc)' }}>
              <h4 style={{ marginTop: 0 }}>Administrador da instituição</h4>
              <p className="field-hint">O colaborador deve existir na plataforma (pré-cadastro).</p>
              <div className="form-grid">
                <div>
                  <label>CPF do administrador</label>
                  <input
                    value={cpfAdministrador}
                    onChange={(e) => setCpfAdministrador(maskCPF(e.target.value))}
                    placeholder="000.000.000-00"
                  />
                </div>
                <div>
                  <label>Nome</label>
                  <input disabled value={nomeAdministrador} placeholder="Preenchido após salvar" />
                </div>
              </div>
              <div className="form-actions form-actions--compact">
                <button type="button" className="btn-secondary" onClick={handleSalvarAdministrador} disabled={loading}>
                  Salvar administrador
                </button>
              </div>
            </div>

            <div className="form-actions form-actions--compact" style={{ marginTop: '1rem' }}>
              <span className="field-hint" style={{ marginRight: 'auto' }}>
                Cadastro ativo: <strong>{cadastroAtivo ? 'Sim' : 'Não'}</strong>
                {trialUtilizado ? ' · Teste grátis já utilizado' : ''}
              </span>
              {cadastroAtivo ? (
                <button type="button" className="btn-danger" onClick={handleDesativarCadastro} disabled={loading}>
                  Desativar cadastro
                </button>
              ) : (
                <button type="button" className="btn-secondary" onClick={handleAtivarCadastro} disabled={loading || !plano}>
                  Ativar cadastro
                </button>
              )}
            </div>

            <div className="form-actions">
              <button type="button" className="btn-primary" onClick={handleSalvar} disabled={loading}>
                {loading ? 'Processando...' : 'Salvar alterações'}
              </button>
            </div>
          </div>
        </>
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

export default GerenciarAcademia;

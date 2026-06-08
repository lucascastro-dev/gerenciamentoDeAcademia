import React, { useCallback, useEffect, useState } from 'react';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import ListaConsultaPessoas, { PessoaListagemItem } from '../common/ListaConsultaPessoas';
import PageShell from '../common/PageShell';
import { carregarSessao, isModoPlataforma } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';

const TIPOS = [
  'ADMINISTRADOR', 'TI', 'FINANCEIRO', 'RH', 'RECEPCIONISTA', 'PROFESSOR',
  'ESTAGIARIO', 'SERVICOS_GERAIS', 'TERCEIRIZADO',
] as const;

interface VinculoInstituicao {
  vinculoId?: number;
  instituicaoId: number;
  razaoSocial: string;
  tipoFuncionario: string;
  areaTerceirizado?: string;
  especializacao?: string;
  cargo?: string;
}

const GerenciarFuncionario: React.FC = () => {
  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);

  const [tela, setTela] = useState<'lista' | 'detalhe'>('lista');
  const [lista, setLista] = useState<PessoaListagemItem[]>([]);
  const [carregandoLista, setCarregandoLista] = useState(true);

  const [cpf, setCpf] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setNascimento] = useState('');
  const [vinculos, setVinculos] = useState<VinculoInstituicao[]>([]);
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

  const carregarLista = useCallback(() => {
    setCarregandoLista(true);
    HttpService.listarFuncionariosResumo()
      .then((r) => setLista(r.data || []))
      .catch(() => setLista([]))
      .finally(() => setCarregandoLista(false));
  }, []);

  useEffect(() => {
    carregarLista();
  }, [carregarLista]);

  const resetForm = () => {
    setCpf('');
    setNome('');
    setRg('');
    setNascimento('');
    setVinculos([]);
    setEndereco(enderecoVazio());
    setTelefone('');
    setEmail('');
    setGerenciarFuncoes(false);
    setCadastroAtivo(false);
    setIsEditable(false);
  };

  const voltarLista = () => {
    resetForm();
    setTela('lista');
  };

  const preencherDetalhe = (data: Record<string, unknown>) => {
    setNome(String(data.nome ?? ''));
    setRg(String(data.rg ?? ''));
    setNascimento(String(data.dataDeNascimento ?? ''));
    setEndereco(parseEndereco(String(data.endereco ?? '')));
    setTelefone(String(data.telefone ?? ''));
    setEmail(String(data.email ?? ''));
    setGerenciarFuncoes(Boolean(data.permitirGerenciarFuncoes));
    setCadastroAtivo(Boolean(data.cadastroAtivo));
    const listaVinculos = (data.vinculos as VinculoInstituicao[] | undefined) ?? [];
    setVinculos(listaVinculos.map((v) => ({
      vinculoId: v.vinculoId,
      instituicaoId: v.instituicaoId,
      razaoSocial: v.razaoSocial,
      tipoFuncionario: String(v.tipoFuncionario || v.cargo || 'RECEPCIONISTA').replace(/ /g, '_').toUpperCase(),
      areaTerceirizado: v.areaTerceirizado ? String(v.areaTerceirizado) : 'RH',
      especializacao: String(v.especializacao ?? ''),
      cargo: v.cargo,
    })));
    setIsEditable(true);
  };

  const handleConsultar = async (cpfInformado: string) => {
    if (!localStorage.getItem('@App:token')) {
      setModal({ open: true, success: false, message: 'Sessão expirada. Faça login novamente.' });
      return;
    }
    setLoading(true);
    try {
      const res = await HttpService.consultarFuncionarioPorCpf(onlyNumbers(cpfInformado));
      preencherDetalhe(res.data);
    } catch {
      setModal({ open: true, success: false, message: 'Funcionário não encontrado.' });
      setTela('lista');
    } finally {
      setLoading(false);
    }
  };

  const abrirDetalhe = async (item: PessoaListagemItem) => {
    resetForm();
    const cpfLimpo = item.cpf || onlyNumbers(item.cpfExibicao || '');
    if (cpfLimpo.length < 11) {
      setModal({ open: true, success: false, message: 'Não foi possível identificar o CPF do colaborador.' });
      return;
    }
    setCpf(maskCPF(cpfLimpo));
    setTela('detalhe');
    await handleConsultar(cpfLimpo);
  };

  const atualizarVinculo = (instituicaoId: number, campo: keyof VinculoInstituicao, valor: string) => {
    setVinculos((prev) => prev.map((v) => (
      v.instituicaoId === instituicaoId ? { ...v, [campo]: valor } : v
    )));
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
      setModal({ open: true, success: false, message: extractApiMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  const handleSalvarPessoal = async () => {
    setLoading(true);
    try {
      await HttpService.editarPessoa({
        nome,
        cpf: onlyNumbers(cpf),
        rg,
        dataDeNascimento,
        endereco: serializarEndereco(endereco),
        telefone: onlyNumbers(telefone),
        email,
        cadastroAtivo,
      });
      setModal({ open: true, success: true, message: 'Dados pessoais atualizados com sucesso.' });
      carregarLista();
    } catch (err) {
      setModal({ open: true, success: false, message: extractApiMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  const handleSalvarVinculos = async () => {
    for (const v of vinculos) {
      if (v.tipoFuncionario === 'PROFESSOR' && !v.especializacao?.trim()) {
        setModal({ open: true, success: false, message: `Informe a especialização para ${v.razaoSocial}.` });
        return;
      }
    }
    setLoading(true);
    try {
      for (const v of vinculos) {
        await HttpService.atualizarVinculoFuncionario({
          cpf: onlyNumbers(cpf),
          instituicaoId: v.instituicaoId,
          tipoFuncionario: v.tipoFuncionario,
          areaTerceirizado: v.tipoFuncionario === 'TERCEIRIZADO' ? v.areaTerceirizado : null,
          especializacao: v.tipoFuncionario === 'PROFESSOR' ? v.especializacao : null,
        });
      }
      setModal({ open: true, success: true, message: 'Funções por instituição atualizadas com sucesso.' });
      await handleConsultar(cpf);
      carregarLista();
    } catch (err) {
      setModal({ open: true, success: false, message: extractApiMessage(err) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageShell
      title="Funcionários"
      subtitle={master
        ? 'Listagem de todos os colaboradores da base — uma linha por vínculo institucional'
        : 'Colaboradores vinculados à sua instituição'}
    >
      {tela === 'lista' && (
        <div className="card">
          <ListaConsultaPessoas
            itens={lista}
            carregando={carregandoLista}
            exibirCargo
            exibirInstituicao={master}
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

          <div className="card" style={{ marginBottom: '1rem' }}>
            <h3 style={{ marginTop: 0 }}>Função por instituição</h3>
            {vinculos.length === 0 ? (
              <p className="field-hint">Nenhum vínculo institucional encontrado.</p>
            ) : (
              vinculos.map((v) => (
                <div
                  key={v.instituicaoId}
                  className="turmas-item"
                  style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--color-border, #e2e8f0)' }}
                >
                  <strong>{v.razaoSocial}</strong>
                  <div className="form-grid" style={{ marginTop: '0.75rem' }}>
                    <div>
                      <label>Cargo / perfil</label>
                      <select
                        value={v.tipoFuncionario}
                        onChange={(e) => atualizarVinculo(v.instituicaoId, 'tipoFuncionario', e.target.value)}
                      >
                        {TIPOS.map((t) => (
                          <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                        ))}
                      </select>
                    </div>
                    {v.tipoFuncionario === 'TERCEIRIZADO' && (
                      <div>
                        <label>Área do terceirizado</label>
                        <select
                          value={v.areaTerceirizado || 'RH'}
                          onChange={(e) => atualizarVinculo(v.instituicaoId, 'areaTerceirizado', e.target.value)}
                        >
                          <option value="RH">Apoio RH</option>
                          <option value="PROFESSOR_SUBSTITUTO">Professor substituto</option>
                          <option value="TI">Apoio TI</option>
                        </select>
                      </div>
                    )}
                    {v.tipoFuncionario === 'PROFESSOR' && (
                      <div>
                        <label>Especialização</label>
                        <input
                          value={v.especializacao || ''}
                          onChange={(e) => atualizarVinculo(v.instituicaoId, 'especializacao', e.target.value)}
                        />
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}
            {vinculos.length > 0 && (
              <div className="form-actions">
                <button type="button" className="btn-primary" onClick={handleSalvarVinculos} disabled={loading}>
                  {loading ? 'Processando...' : 'Salvar funções por instituição'}
                </button>
              </div>
            )}
          </div>

          <div className="card">
            <h3 style={{ marginTop: 0 }}>Dados pessoais</h3>
            <div className="form-grid">
              <div className="form-grid__span-2"><label>Nome</label><input value={nome} onChange={(e) => setNome(e.target.value)} /></div>
              <div><label>CPF</label><input disabled value={cpf} /></div>
              <div><label>RG</label><input value={rg} onChange={(e) => setRg(e.target.value)} /></div>
              <div><label>Nascimento</label><input type="date" value={dataDeNascimento} onChange={(e) => setNascimento(e.target.value)} /></div>
              <div><label>Telefone</label><input type="tel" value={telefone} onChange={(e) => setTelefone(maskPhone(e.target.value))} /></div>
              <div>
                <label>E-mail</label>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nome@email.com" />
              </div>
            </div>
            <EnderecoFields value={endereco} onChange={setEndereco} />
            <div className="form-grid" style={{ marginTop: '0.75rem' }}>
              <label className="switch-inline">
                <input type="checkbox" checked={cadastroAtivo} onChange={(e) => setCadastroAtivo(e.target.checked)} />
                Cadastro ativo
              </label>
              {permitirGerenciarFuncoes && (
                <p className="field-hint">Operador sub-master da plataforma (não pode criar outros masters).</p>
              )}
            </div>
            <div className="form-actions">
              {sessao?.masterRaiz && (
                <button type="button" className="btn-secondary" onClick={toggleSubMaster} disabled={loading}>
                  {permitirGerenciarFuncoes ? 'Revogar sub-master' : 'Habilitar sub-master'}
                </button>
              )}
              <button type="button" className="btn-primary" onClick={handleSalvarPessoal} disabled={loading}>
                {loading ? 'Processando...' : 'Salvar dados pessoais'}
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

export default GerenciarFuncionario;

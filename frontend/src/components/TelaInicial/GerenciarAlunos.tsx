import { useCallback, useEffect, useState } from 'react';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import ListaConsultaPessoas, { PessoaListagemItem } from '../common/ListaConsultaPessoas';
import PageShell from '../common/PageShell';
import CurrencyInput from '../common/CurrencyInput';
import PhoneInput from '../common/PhoneInput';
import '../common/PhoneFields.css';
import { carregarSessao, isModoPlataforma, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';
import { formatarMoeda, parseMoeda } from '../../utils/moeda';
import { formatarTelefoneExibicao, telefoneParaApi } from '../../utils/phoneFormat';

interface MatriculaInstituicao {
  instituicaoId: number;
  razaoSocial: string;
  valorMensalidade?: number;
  diaVencimentoMensalidade?: number;
  turmas: Array<{ id: number; modalidade: string; horario: string; sala?: string }>;
}

interface FinanceiroInstituicao {
  valor: string;
  dia: string;
}

const str = (v?: string | null) => v ?? '';

const GerenciarAlunos: React.FC = () => {
  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);
  const podeEditarAluno = possuiPermissao(sessao, 'aluno:editar');
  const consultaSomenteLeitura = !podeEditarAluno;

  const [tela, setTela] = useState<'lista' | 'detalhe'>('lista');
  const [lista, setLista] = useState<PessoaListagemItem[]>([]);
  const [carregandoLista, setCarregandoLista] = useState(true);

  const [cpfBusca, setCpfBusca] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setDataDeNascimento] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [nomeResponsavel, setNomeResponsavel] = useState('');
  const [telefoneResponsavel, setTelefoneResponsavel] = useState('');
  const [matriculas, setMatriculas] = useState<MatriculaInstituicao[]>([]);
  const [turmasProfessor, setTurmasProfessor] = useState<Array<{ id: number; modalidade: string; horario: string; sala?: string }>>([]);
  const [financeiroPorInstituicao, setFinanceiroPorInstituicao] = useState<Record<number, FinanceiroInstituicao>>({});
  const [alunoCarregado, setAlunoCarregado] = useState(false);
  const [carregandoDetalhe, setCarregandoDetalhe] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  const maskCPF = (v: string) =>
    v.replace(/\D/g, '')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
      .slice(0, 14);

  const carregarLista = useCallback(() => {
    setCarregandoLista(true);
    HttpService.listarAlunosResumo()
      .then((r) => setLista(r.data || []))
      .catch(() => setLista([]))
      .finally(() => setCarregandoLista(false));
  }, []);

  useEffect(() => {
    carregarLista();
  }, [carregarLista]);

  const limparForm = () => {
    setCpfBusca('');
    setNome('');
    setRg('');
    setDataDeNascimento('');
    setEndereco(enderecoVazio());
    setTelefone('');
    setEmail('');
    setNomeResponsavel('');
    setTelefoneResponsavel('');
    setMatriculas([]);
    setTurmasProfessor([]);
    setFinanceiroPorInstituicao({});
    setAlunoCarregado(false);
  };

  const voltarLista = () => {
    limparForm();
    setTela('lista');
  };

  const payloadPessoal = () => ({
    nome,
    cpf: onlyNumbers(cpfBusca),
    rg,
    dataDeNascimento,
    endereco: serializarEndereco(endereco),
    telefone: telefoneParaApi(telefone),
    email,
    nomeResponsavel,
    telefoneResponsavel: telefoneParaApi(telefoneResponsavel),
  });

  const aplicarFinanceiroDasMatriculas = (listaMat: MatriculaInstituicao[]) => {
    const mapa: Record<number, FinanceiroInstituicao> = {};
    listaMat.forEach((m) => {
      mapa[m.instituicaoId] = {
        valor: m.valorMensalidade != null ? formatarMoeda(m.valorMensalidade) : '',
        dia: String(m.diaVencimentoMensalidade ?? '10'),
      };
    });
    setFinanceiroPorInstituicao(mapa);
  };

  const consultar = async (cpfInformado?: string) => {
    const cpf = onlyNumbers(cpfInformado ?? cpfBusca);
    if (cpf.length < 11) {
      setModal({ open: true, success: false, message: 'CPF inválido para consulta.' });
      return;
    }
    setCpfBusca(maskCPF(cpf));
    try {
      if (consultaSomenteLeitura) {
        const r = await HttpService.consultarAlunoProfessor(cpf);
        const d = r.data;
        setNome(str(d.nome));
        setRg(str(d.rgMascarado));
        setDataDeNascimento(str(d.dataDeNascimento));
        setEndereco(parseEndereco(d.enderecoResumido || ''));
        setTelefone(str(d.telefoneMascarado));
        setEmail(str(d.emailMascarado));
        setNomeResponsavel(str(d.nomeResponsavel));
        setTelefoneResponsavel(str(d.telefoneResponsavelMascarado));
        setMatriculas([]);
        setTurmasProfessor(d.turmasInstituicao || []);
        setFinanceiroPorInstituicao({});
        setAlunoCarregado(true);
        return;
      }

      const r = await HttpService.consultarAlunoPorCpf(cpf);
      const d = r.data;
      setNome(str(d.nome));
      setRg(str(d.rg));
      setDataDeNascimento(str(d.dataDeNascimento));
      setEndereco(parseEndereco(d.endereco));
      setTelefone(formatarTelefoneExibicao(str(d.telefone)));
      setEmail(str(d.email));
      setNomeResponsavel(str(d.nomeResponsavel));
      setTelefoneResponsavel(formatarTelefoneExibicao(str(d.telefoneResponsavel)));
      const mats = d.matriculas || [];
      setMatriculas(mats);
      aplicarFinanceiroDasMatriculas(mats);
      setAlunoCarregado(true);
    } catch (e) {
      setAlunoCarregado(false);
      setMatriculas([]);
      setTurmasProfessor([]);
      setFinanceiroPorInstituicao({});
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Aluno não encontrado.') });
    }
  };

  const abrirDetalhe = async (item: PessoaListagemItem) => {
    limparForm();
    setTela('detalhe');
    setCarregandoDetalhe(true);
    try {
      if (consultaSomenteLeitura) {
        const r = await HttpService.consultarAlunoProfessorPorId(item.id);
        const d = r.data;
        setNome(str(d.nome));
        setRg(str(d.rgMascarado));
        setDataDeNascimento(str(d.dataDeNascimento));
        setEndereco(parseEndereco(d.enderecoResumido || ''));
        setTelefone(str(d.telefoneMascarado));
        setEmail(str(d.emailMascarado));
        setNomeResponsavel(str(d.nomeResponsavel));
        setTelefoneResponsavel(str(d.telefoneResponsavelMascarado));
        setTurmasProfessor(d.turmasInstituicao || []);
        setCpfBusca(d.cpfMascarado || item.cpfExibicao || '');
        setAlunoCarregado(true);
        return;
      }
      const cpf = item.cpf || onlyNumbers(item.cpfExibicao || '');
      if (cpf.length < 11) {
        setModal({ open: true, success: false, message: 'Não foi possível identificar o CPF do aluno.' });
        setTela('lista');
        return;
      }
      await consultar(cpf);
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Aluno não encontrado.') });
      setTela('lista');
    } finally {
      setCarregandoDetalhe(false);
    }
  };

  const atualizarFinanceiroInstituicao = (instituicaoId: number, campo: 'valor' | 'dia', valor: string) => {
    setFinanceiroPorInstituicao((prev) => ({
      ...prev,
      [instituicaoId]: {
        valor: campo === 'valor' ? valor : (prev[instituicaoId]?.valor ?? ''),
        dia: campo === 'dia' ? valor : (prev[instituicaoId]?.dia ?? '10'),
      },
    }));
  };

  const salvarAlteracoes = async () => {
    if (!alunoCarregado) {
      setModal({ open: true, success: false, message: 'Nenhum aluno selecionado.' });
      return;
    }
    const cpf = onlyNumbers(cpfBusca);
    if (cpf.length < 11) {
      setModal({ open: true, success: false, message: 'CPF inválido. Reabra o aluno pela lista.' });
      return;
    }
    try {
      const base = payloadPessoal();
      if (matriculas.length === 0) {
        await HttpService.alterarAluno(base);
      } else {
        for (const m of matriculas) {
          const fin = financeiroPorInstituicao[m.instituicaoId];
          await HttpService.alterarAluno({
            ...base,
            cpf,
            instituicaoId: m.instituicaoId,
            valorMensalidade: fin ? parseMoeda(fin.valor) : m.valorMensalidade,
            diaVencimentoMensalidade: fin ? parseInt(fin.dia, 10) : m.diaVencimentoMensalidade,
          });
        }
      }
      setModal({ open: true, success: true, message: 'Dados do aluno atualizados com sucesso.' });
      await consultar(cpf);
      carregarLista();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  const desmatricular = async () => {
    if (!window.confirm('Desmatricular remove o cadastro do aluno em todo o sistema. Continuar?')) return;
    try {
      await HttpService.desmatricularAluno(onlyNumbers(cpfBusca));
      setModal({ open: true, success: true, message: 'Aluno desmatriculado.' });
      voltarLista();
      carregarLista();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  return (
    <PageShell
      title="Consultar alunos"
      subtitle={consultaSomenteLeitura
        ? 'Consulta somente leitura de alunos matriculados na instituição'
        : master
          ? 'Listagem de todos os alunos da base — busca e paginação na tabela'
          : 'Alunos matriculados na sua instituição'}
    >
      {tela === 'lista' && (
        <div className="card">
          <ListaConsultaPessoas
            itens={lista}
            carregando={carregandoLista}
            onVerDetalhes={abrirDetalhe}
          />
        </div>
      )}

      {tela === 'detalhe' && carregandoDetalhe && (
        <p className="field-hint">Carregando dados do aluno...</p>
      )}

      {tela === 'detalhe' && alunoCarregado && (
        <>
          <div className="form-actions" style={{ marginBottom: '1rem' }}>
            <button type="button" className="btn-secondary" onClick={voltarLista}>
              ← Voltar à lista
            </button>
          </div>

          {consultaSomenteLeitura && turmasProfessor.length > 0 && (
            <div className="card" style={{ marginBottom: '1rem' }}>
              <h3 style={{ marginTop: 0 }}>Turmas na instituição</h3>
              <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                {turmasProfessor.map((t) => (
                  <li key={t.id}>
                    {t.modalidade} — {t.horario}{t.sala ? ` · ${t.sala}` : ''}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {!consultaSomenteLeitura && (
            <div className="card" style={{ marginBottom: '1rem' }}>
              <h3 style={{ marginTop: 0 }}>Matrículas e mensalidade por instituição</h3>
              {matriculas.length === 0 ? (
                <p className="field-hint">Nenhuma turma vinculada em instituições gerenciadas.</p>
              ) : (
                matriculas.map((m) => {
                  const fin = financeiroPorInstituicao[m.instituicaoId] ?? { valor: '', dia: '10' };
                  return (
                    <div key={m.instituicaoId} className="turmas-item" style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--color-border, #e2e8f0)' }}>
                      <strong>{m.razaoSocial}</strong>
                      <div className="form-grid" style={{ marginTop: '0.75rem' }}>
                        <div>
                          <label>Mensalidade</label>
                          <CurrencyInput
                            value={fin.valor}
                            onChange={(_n, formatado) => atualizarFinanceiroInstituicao(m.instituicaoId, 'valor', formatado)}
                          />
                        </div>
                        <div>
                          <label>Dia venc.</label>
                          <input
                            type="number"
                            min={1}
                            max={28}
                            value={fin.dia}
                            onChange={(e) => atualizarFinanceiroInstituicao(m.instituicaoId, 'dia', e.target.value)}
                          />
                        </div>
                      </div>
                      {m.turmas.length === 0 ? (
                        <p className="field-hint" style={{ margin: '0.35rem 0 0' }}>Sem turmas ativas nesta instituição.</p>
                      ) : (
                        <ul style={{ margin: '0.5rem 0 0', paddingLeft: '1.2rem' }}>
                          {m.turmas.map((t) => (
                            <li key={t.id}>
                              {t.modalidade} — {t.horario}
                              {t.sala ? ` · ${t.sala}` : ''}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  );
                })
              )}
            </div>
          )}

          <div className="card" style={{ marginBottom: '1rem' }}>
            <h3 style={{ marginTop: 0 }}>Dados do aluno</h3>
            <div className="form-grid">
              <div><label>Nome</label><input value={nome} readOnly={consultaSomenteLeitura} onChange={(e) => setNome(e.target.value)} /></div>
              <div><label>CPF</label><input value={cpfBusca} readOnly /></div>
              <div><label>RG</label><input value={rg} readOnly={consultaSomenteLeitura} onChange={(e) => setRg(e.target.value)} /></div>
              <div><label>Nascimento</label><input type="date" value={dataDeNascimento} readOnly={consultaSomenteLeitura} onChange={(e) => setDataDeNascimento(e.target.value)} /></div>
              <PhoneInput label="Telefone" value={telefone} onChange={setTelefone} readOnly={consultaSomenteLeitura} />
              <div>
                <label>E-mail</label>
                <input type="email" value={email} readOnly={consultaSomenteLeitura} onChange={(e) => setEmail(e.target.value)} placeholder="seuemail@exemplo.com" />
              </div>
              <div><label>Responsável</label><input value={nomeResponsavel} readOnly={consultaSomenteLeitura} onChange={(e) => setNomeResponsavel(e.target.value)} /></div>
              <PhoneInput label="Tel. responsável" value={telefoneResponsavel} onChange={setTelefoneResponsavel} readOnly={consultaSomenteLeitura} />
            </div>
            <EnderecoFields value={endereco} onChange={setEndereco} disabled={consultaSomenteLeitura} />
            {!consultaSomenteLeitura && (
              <div className="form-actions">
                <button type="button" className="btn-primary" onClick={salvarAlteracoes}>Salvar alterações</button>
                <button type="button" className="btn-danger" onClick={desmatricular}>Desmatricular</button>
              </div>
            )}
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

export default GerenciarAlunos;

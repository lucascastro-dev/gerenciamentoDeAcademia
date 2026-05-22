import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { carregarSessao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';

const GerenciarAlunos: React.FC = () => {
  const [cpfBusca, setCpfBusca] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setDataDeNascimento] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [valorMensalidade, setValorMensalidade] = useState('');
  const [diaVencimento, setDiaVencimento] = useState('10');
  const [nomeResponsavel, setNomeResponsavel] = useState('');
  const [telefoneResponsavel, setTelefoneResponsavel] = useState('');
  const [lista, setLista] = useState<{ cpf: string; nome: string }[]>([]);
  const [alunoCarregado, setAlunoCarregado] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  const carregarLista = () => {
    const instituicaoId = carregarSessao()?.vinculo;
    if (!instituicaoId) {
      setLista([]);
      return;
    }
    HttpService.listarAlunos(instituicaoId)
      .then((r) => setLista(r.data || []))
      .catch(() => setLista([]));
  };

  useEffect(() => { carregarLista(); }, []);

  const limparForm = () => {
    setCpfBusca('');
    setNome('');
    setRg('');
    setDataDeNascimento('');
    setEndereco(enderecoVazio());
    setTelefone('');
    setValorMensalidade('');
    setDiaVencimento('10');
    setNomeResponsavel('');
    setTelefoneResponsavel('');
    setAlunoCarregado(false);
  };

  const payload = () => ({
    nome,
    cpf: onlyNumbers(cpfBusca),
    rg,
    dataDeNascimento,
    endereco: serializarEndereco(endereco),
    telefone: onlyNumbers(telefone),
    valorMensalidade: parseFloat(valorMensalidade),
    diaVencimentoMensalidade: parseInt(diaVencimento, 10),
    nomeResponsavel,
    telefoneResponsavel: onlyNumbers(telefoneResponsavel),
  });

  const consultar = async (cpfInformado?: string) => {
    const cpf = onlyNumbers(cpfInformado ?? cpfBusca);
    if (cpf.length < 11) {
      setModal({ open: true, success: false, message: 'Informe um CPF válido com 11 dígitos.' });
      return;
    }
    setCpfBusca(cpf);
    try {
      const instituicaoId = carregarSessao()?.vinculo;
      if (!instituicaoId) {
        setModal({ open: true, success: false, message: 'Instituição não identificada na sessão.' });
        return;
      }
      const r = await HttpService.consultarAluno(cpf, instituicaoId);
      const d = r.data;
      setNome(d.nome);
      setRg(d.rg);
      setDataDeNascimento(d.dataDeNascimento);
      setEndereco(parseEndereco(d.endereco));
      setTelefone(d.telefone);
      setValorMensalidade(String(d.valorMensalidade ?? ''));
      setDiaVencimento(String(d.diaVencimentoMensalidade ?? '10'));
      setNomeResponsavel(d.nomeResponsavel ?? '');
      setTelefoneResponsavel(d.telefoneResponsavel ?? '');
      setAlunoCarregado(true);
    } catch (e) {
      setAlunoCarregado(false);
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Aluno não encontrado.') });
    }
  };

  const salvarAlteracoes = async () => {
    if (!alunoCarregado) {
      setModal({ open: true, success: false, message: 'Consulte um aluno pelo CPF antes de salvar.' });
      return;
    }
    try {
      await HttpService.alterarAluno(payload());
      setModal({ open: true, success: true, message: 'Dados do aluno atualizados com sucesso.' });
      carregarLista();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  const desmatricular = async () => {
    try {
      await HttpService.desmatricularAluno(onlyNumbers(cpfBusca));
      setModal({ open: true, success: true, message: 'Aluno desmatriculado.' });
      limparForm();
      carregarLista();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  return (
    <PageShell
      title="Consultar alunos"
      subtitle="Busque pelo CPF, edite dados ou matricule um novo aluno pelo menu dedicado"
    >
      <div className="card" style={{ marginBottom: '1rem' }}>
        <div className="form-grid">
          <div>
            <label>CPF</label>
            <input
              value={cpfBusca}
              onChange={(e) => setCpfBusca(e.target.value)}
              placeholder="Somente números ou formatado"
            />
          </div>
        </div>
        <div className="form-actions">
          <button type="button" className="btn-primary" onClick={() => consultar()}>Consultar</button>
          <button type="button" className="btn-secondary" onClick={limparForm}>Limpar</button>
          <Link to="/arealogada/matricula" className="btn-secondary" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center' }}>
            Matricular novo aluno
          </Link>
        </div>
      </div>

      {alunoCarregado && (
        <div className="card" style={{ marginBottom: '1rem' }}>
          <h3 style={{ marginTop: 0 }}>Dados do aluno</h3>
          <div className="form-grid">
            <div><label>Nome</label><input value={nome} onChange={(e) => setNome(e.target.value)} /></div>
            <div><label>RG</label><input value={rg} onChange={(e) => setRg(e.target.value)} /></div>
            <div><label>Nascimento</label><input type="date" value={dataDeNascimento} onChange={(e) => setDataDeNascimento(e.target.value)} /></div>
            <div><label>Telefone</label><input value={telefone} onChange={(e) => setTelefone(e.target.value)} /></div>
            <div><label>Mensalidade</label><input type="number" value={valorMensalidade} onChange={(e) => setValorMensalidade(e.target.value)} /></div>
            <div><label>Dia venc.</label><input type="number" min={1} max={28} value={diaVencimento} onChange={(e) => setDiaVencimento(e.target.value)} /></div>
            <div><label>Responsável</label><input value={nomeResponsavel} onChange={(e) => setNomeResponsavel(e.target.value)} /></div>
            <div><label>Tel. responsável</label><input value={telefoneResponsavel} onChange={(e) => setTelefoneResponsavel(e.target.value)} /></div>
          </div>
          <EnderecoFields value={endereco} onChange={setEndereco} />
          <div className="form-actions">
            <button type="button" className="btn-primary" onClick={salvarAlteracoes}>Salvar alterações</button>
            <button type="button" className="btn-danger" onClick={desmatricular}>Desmatricular</button>
          </div>
        </div>
      )}

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Alunos cadastrados ({lista.length})</h3>
        {lista.length === 0 ? (
          <p className="field-hint">Nenhum aluno listado.</p>
        ) : (
          <table className="programacao-table">
            <thead>
              <tr><th>Nome</th><th>CPF</th><th /></tr>
            </thead>
            <tbody>
              {lista.map((a) => (
                <tr key={a.cpf}>
                  <td>{a.nome}</td>
                  <td>{a.cpf}</td>
                  <td>
                    <button type="button" className="btn-secondary" onClick={() => consultar(a.cpf)}>
                      Abrir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
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

export default GerenciarAlunos;

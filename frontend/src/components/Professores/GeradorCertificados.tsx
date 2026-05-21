import { useEffect, useState } from 'react';
import { carregarSessao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import './GeradorCertificados.css';

interface AlunoCert {
  nome: string;
  faixa: string;
  medida: string;
}

const PROFESSORES = ['Marcelo', 'Junior', 'Lucas', 'Kessia'];
const FAIXAS = [
  'Branca Ponta Cinza', 'Cinza', 'Cinza Ponta Azul', 'Azul', 'Azul Ponta Amarela',
  'Amarela', 'Amarela Ponta Laranja', 'Laranja', 'Verde', 'Roxa', 'Marrom', 'Preta',
];
const MEDIDAS = ['M000', 'M00', 'M0', 'M1', 'M2', 'M3', 'M4', 'A1', 'A2', 'A3', 'A4'];
const PROJETOS = [
  { value: 'AMADOM', label: 'AMADOM' },
  { value: 'SCTJ', label: 'SC Team Judô' },
  { value: 'SESV', label: 'SESV' },
];

const GeradorCertificados: React.FC = () => {
  const [professor, setProfessor] = useState('');
  const [dataEvento, setDataEvento] = useState('');
  const [personalizado, setPersonalizado] = useState(false);
  const [projeto, setProjeto] = useState('AMADOM');
  const [nomeAluno, setNomeAluno] = useState('');
  const [faixa, setFaixa] = useState('');
  const [medida, setMedida] = useState('');
  const [alunos, setAlunos] = useState<AlunoCert[]>([]);
  const [editIndex, setEditIndex] = useState<number | null>(null);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const sessao = carregarSessao();
    if (sessao?.nome && sessao.tipoFuncionario === 'PROFESSOR' && !professor) {
      const match = PROFESSORES.find((p) => p.toLowerCase() === sessao.nome.split(' ')[0]?.toLowerCase());
      if (match) setProfessor(match);
    }
  }, []);

  const professorBloqueado = alunos.length > 0;

  const adicionarAluno = () => {
    if (!professor) {
      setModal({ open: true, success: false, message: 'Selecione o professor antes de adicionar alunos.' });
      return;
    }
    if (!nomeAluno.trim() || !faixa || !medida) {
      setModal({ open: true, success: false, message: 'Preencha nome, faixa e tamanho da faixa.' });
      return;
    }
    const item = { nome: nomeAluno.trim(), faixa, medida };
    if (editIndex !== null) {
      setAlunos((prev) => prev.map((a, i) => (i === editIndex ? item : a)));
      setEditIndex(null);
    } else {
      setAlunos((prev) => [...prev, item]);
    }
    setNomeAluno('');
    setFaixa('');
    setMedida('');
  };

  const editarAluno = (index: number) => {
    const a = alunos[index];
    setNomeAluno(a.nome);
    setFaixa(a.faixa);
    setMedida(a.medida);
    setEditIndex(index);
  };

  const removerAluno = (index: number) => setAlunos((prev) => prev.filter((_, i) => i !== index));
  const removerTodos = () => { setAlunos([]); setEditIndex(null); };

  const gerar = async () => {
    if (!professor || !dataEvento || alunos.length === 0) {
      setModal({ open: true, success: false, message: 'Selecione professor, data e adicione ao menos um aluno.' });
      return;
    }
    setLoading(true);
    try {
      const { data } = await HttpService.gerarCertificados({
        professor, dataEvento, personalizado, projeto: personalizado ? projeto : null, alunos,
      });
      setModal({
        open: true,
        success: true,
        message: typeof data === 'string' ? data : 'Certificados gerados com sucesso.',
      });
      setAlunos([]);
      setDataEvento('');
      setPersonalizado(false);
      setProfessor('');
    } catch {
      setModal({ open: true, success: false, message: 'Falha ao gerar. Verifique login, templates e permissões.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageShell title="Gerador de certificados">
      <div className="card cert-form">
        <div className="form-grid form-grid--cert">
          <div>
            <label htmlFor="professor">Professor</label>
            <select id="professor" value={professor} disabled={professorBloqueado} onChange={(e) => setProfessor(e.target.value)}>
              <option value="" disabled>Selecione o professor</option>
              {PROFESSORES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div>
            <label htmlFor="nome">Nome do aluno</label>
            <input id="nome" type="text" placeholder="Nome do aluno" value={nomeAluno} onChange={(e) => setNomeAluno(e.target.value)} />
          </div>
          <div>
            <label htmlFor="faixa">Faixa</label>
            <select id="faixa" value={faixa} onChange={(e) => setFaixa(e.target.value)}>
              <option value="" disabled>Selecione a faixa</option>
              {FAIXAS.map((f) => <option key={f} value={f}>{f}</option>)}
            </select>
          </div>
          <div>
            <label htmlFor="medida">Tamanho</label>
            <select id="medida" value={medida} onChange={(e) => setMedida(e.target.value)}>
              <option value="" disabled>Tamanho</option>
              {MEDIDAS.map((m) => <option key={m} value={m}>{m}</option>)}
            </select>
          </div>
        </div>

        <div className="form-actions form-actions--compact">
          <button type="button" className="btn-primary" onClick={adicionarAluno}>
            {editIndex !== null ? 'Salvar' : 'Adicionar'}
          </button>
          <button type="button" className="btn-danger" onClick={removerTodos}>Excluir todos</button>
        </div>

        {alunos.length > 0 && (
          <div className="cert-form__table-wrap table-wrap">
            <table className="cert-form__table">
              <thead>
                <tr><th>Nome</th><th>Faixa</th><th>Tamanho</th><th /></tr>
              </thead>
              <tbody>
                {alunos.map((a, i) => (
                  <tr key={`${a.nome}-${i}`}>
                    <td>{a.nome}</td><td>{a.faixa}</td><td>{a.medida}</td>
                    <td>
                      <button type="button" className="btn-sm btn-sm--edit" onClick={() => editarAluno(i)}>Editar</button>
                      <button type="button" className="btn-sm btn-sm--remove" onClick={() => removerAluno(i)}>×</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <div className="form-grid form-grid--cert" style={{ marginTop: '1rem' }}>
          <div>
            <label htmlFor="dataEvento">Data do evento</label>
            <input id="dataEvento" type="date" value={dataEvento} onChange={(e) => setDataEvento(e.target.value)} />
          </div>
          {personalizado && (
            <div>
              <label htmlFor="projetos">Projeto / logo</label>
              <select id="projetos" value={projeto} onChange={(e) => setProjeto(e.target.value)}>
                {PROJETOS.map((p) => <option key={p.value} value={p.value}>{p.label}</option>)}
              </select>
            </div>
          )}
        </div>

        <label className="cert-form__checkbox switch-inline">
          <input type="checkbox" checked={personalizado} onChange={(e) => setPersonalizado(e.target.checked)} />
          Certificado personalizado com logo
        </label>

        <div className="form-actions form-actions--compact">
          <button type="button" className="btn-primary" disabled={loading} onClick={gerar}>
            {loading ? 'Gerando...' : 'Gerar certificados'}
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

export default GeradorCertificados;

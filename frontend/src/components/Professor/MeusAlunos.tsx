import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import HttpService from '../../services/HttpService';

const MeusAlunos: React.FC = () => {
  const [params] = useSearchParams();
  const [turmas, setTurmas] = useState<any[]>([]);
  const [alunos, setAlunos] = useState<any[]>([]);
  const [turmaId, setTurmaId] = useState(params.get('turma') || '');

  useEffect(() => {
    HttpService.minhasTurmasProfessor().then((r) => setTurmas(r.data));
  }, []);

  useEffect(() => {
    if (!turmaId) {
      setAlunos([]);
      return;
    }
    HttpService.alunosDaTurma(turmaId).then((r) => setAlunos(r.data));
  }, [turmaId]);

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>Alunos das minhas turmas</h2>
      <div className="card" style={{ marginBottom: '1rem' }}>
        <label>Selecione a turma</label>
        <select value={turmaId} onChange={(e) => setTurmaId(e.target.value)}>
          <option value="">—</option>
          {turmas.map((t) => (
            <option key={t.id} value={t.id}>{t.modalidade} ({t.horario})</option>
          ))}
        </select>
      </div>
      <div className="card">
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid #e2e8f0' }}>
              <th align="left">Nome</th><th align="left">CPF</th><th align="left">Telefone</th>
            </tr>
          </thead>
          <tbody>
            {alunos.map((a) => (
              <tr key={a.cpf}><td>{a.nome}</td><td>{a.cpf}</td><td>{a.telefone}</td></tr>
            ))}
          </tbody>
        </table>
        {turmaId && alunos.length === 0 && <p>Nenhum aluno nesta turma.</p>}
      </div>
    </div>
  );
};

export default MeusAlunos;

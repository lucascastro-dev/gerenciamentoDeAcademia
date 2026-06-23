import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import HttpService from '../../services/HttpService';

interface TurmaResumo {
  id: number;
  modalidade: string;
  horario: string;
  sala?: string;
}

const MinhasTurmas: React.FC = () => {
  const [turmas, setTurmas] = useState<TurmaResumo[]>([]);

  useEffect(() => {
    HttpService.minhasTurmasProfessor().then((r) => setTurmas(r.data));
  }, []);

  return (
    <div>
      <h2 style={{ marginTop: 0 }}>Minhas turmas</h2>
      <div className="dashboard-grid">
        {turmas.map((t) => (
          <div key={t.id} className="card">
            <h3 style={{ margin: '0 0 0.5rem' }}>{t.modalidade}</h3>
            <p style={{ margin: 0, color: 'var(--color-muted)' }}>{t.horario} — {t.dias?.join(', ')}</p>
            <p>{t.totalAlunos} aluno(s)</p>
            <Link to={`/arealogada/professor/alunos?turma=${t.id}`}>Ver alunos</Link>
          </div>
        ))}
      </div>
      {turmas.length === 0 && <div className="card"><p>Nenhuma turma vinculada ao seu CPF.</p></div>}
    </div>
  );
};

export default MinhasTurmas;

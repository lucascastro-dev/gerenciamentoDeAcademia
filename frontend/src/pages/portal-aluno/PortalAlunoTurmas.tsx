import { useEffect, useState } from 'react';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';

interface TurmaAluno {
  id: number;
  modalidade: string;
  horario: string;
  sala?: string;
  professorNome?: string;
}

const PortalAlunoTurmas: React.FC = () => {
  const [turmas, setTurmas] = useState<TurmaAluno[]>([]);

  useEffect(() => {
    HttpService.portalAlunoTurmas().then((r) => setTurmas(r.data)).catch(() => setTurmas([]));
  }, []);

  return (
    <PageShell title="Minhas turmas">
      <div className="card">
        {turmas.length === 0 && <p>Você ainda não está matriculado em nenhuma turma.</p>}
        {turmas.map((t) => (
          <div key={t.id} className="turma-item">
            <strong>{t.modalidade}</strong> — {t.horario}
            <div className="field-hint">Professor: {t.professorNome || 'A definir'}</div>
          </div>
        ))}
      </div>
    </PageShell>
  );
};

export default PortalAlunoTurmas;

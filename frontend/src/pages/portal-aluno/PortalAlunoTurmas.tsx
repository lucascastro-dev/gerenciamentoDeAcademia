import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { COPY_UI } from '../../constants/copy';
import '../../theme/portal-aluno.css';

interface TurmaAluno {
  id: number;
  modalidade: string;
  horario: string;
  horaInicio?: string;
  horaFim?: string;
  sala?: string;
  dias?: string[];
  professorNome?: string;
  professorEspecializacao?: string;
}

const DIAS_ABREV: Record<string, string> = {
  Segunda: 'Seg',
  Terça: 'Ter',
  Quarta: 'Qua',
  Quinta: 'Qui',
  Sexta: 'Sex',
  Sábado: 'Sáb',
  Domingo: 'Dom',
};

function abreviarDia(dia: string): string {
  return DIAS_ABREV[dia] ?? dia.slice(0, 3);
}

const PortalAlunoTurmas: React.FC = () => {
  const [turmas, setTurmas] = useState<TurmaAluno[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    HttpService.portalAlunoTurmas()
      .then((r) => setTurmas(r.data || []))
      .catch((e) => {
        setErro(extractApiMessage(e, COPY_UI.portalAluno.turmasErro));
        setTurmas([]);
      })
      .finally(() => setCarregando(false));
  }, []);

  const horarioExibicao = (t: TurmaAluno) => {
    if (t.horaInicio && t.horaFim) return `${t.horaInicio} – ${t.horaFim}`;
    return t.horario || 'Horário a confirmar';
  };

  return (
    <PageShell
      title={COPY_UI.portalAluno.turmasTitulo}
      subtitle={COPY_UI.portalAluno.turmasSubtitulo}
    >
      <div className="portal-aluno-page">
        {erro && <div className="portal-aluno-alert" role="alert">{erro}</div>}

        {carregando && (
          <div className="card">
            <p className="field-hint">{COPY_UI.portalAluno.turmasCarregando}</p>
          </div>
        )}

        {!carregando && turmas.length === 0 && !erro && (
          <div className="card portal-aluno-empty">
            <div className="portal-aluno-empty__icon" aria-hidden="true">📚</div>
            <h3>{COPY_UI.portalAluno.turmasVazioTitulo}</h3>
            <p>{COPY_UI.portalAluno.turmasVazioTexto}</p>
          </div>
        )}

        {!carregando && turmas.length > 0 && (
          <>
            <div className="portal-aluno-stats">
              <div className="portal-aluno-stat">
                <strong>{turmas.length}</strong>
                <span>{turmas.length === 1 ? 'Turma ativa' : 'Turmas ativas'}</span>
              </div>
              <div className="portal-aluno-stat">
                <strong>{turmas.filter((t) => t.professorNome).length}</strong>
                <span>Com professor definido</span>
              </div>
            </div>

            <div className="portal-turmas-grid">
              {turmas.map((t) => (
                <article key={t.id} className="portal-turma-card">
                  <div className="portal-turma-card__head">
                    <h3>{t.modalidade}</h3>
                    <span className="portal-turma-card__badge">Matriculado</span>
                  </div>

                  <div className="portal-turma-card__meta">
                    <div className="portal-turma-card__meta-row">
                      <span aria-hidden="true">🕐</span>
                      <span>{horarioExibicao(t)}</span>
                    </div>
                    {t.sala && (
                      <div className="portal-turma-card__meta-row">
                        <span aria-hidden="true">📍</span>
                        <span>{t.sala}</span>
                      </div>
                    )}
                    {t.dias && t.dias.length > 0 && (
                      <div className="portal-turma-card__dias">
                        {t.dias.map((d) => (
                          <span key={d} className="portal-turma-card__dia">{abreviarDia(d)}</span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className="portal-turma-card__prof">
                    Professor:{' '}
                    <strong>{t.professorNome || 'A definir'}</strong>
                    {t.professorEspecializacao && (
                      <span className="field-hint"> · {t.professorEspecializacao}</span>
                    )}
                  </div>
                </article>
              ))}
            </div>

            <div className="portal-aluno-quick">
              <Link to="/arealogada/aluno/programacao">
                <span className="portal-aluno-quick__icon" aria-hidden="true">📅</span>
                Ver minha programação
              </Link>
              <Link to="/arealogada/aluno/mensalidades">
                <span className="portal-aluno-quick__icon" aria-hidden="true">💳</span>
                Mensalidades
              </Link>
              <Link to="/arealogada/aluno/dados">
                <span className="portal-aluno-quick__icon" aria-hidden="true">👤</span>
                Meus dados
              </Link>
            </div>
          </>
        )}
      </div>
    </PageShell>
  );
};

export default PortalAlunoTurmas;

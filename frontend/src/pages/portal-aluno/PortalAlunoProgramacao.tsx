import { useEffect, useMemo, useState } from 'react';
import { COPY_UI } from '../../constants/copy';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import '../../theme/programacao.css';

interface ItemProgramacao {
  id: number;
  escopoLancamento?: string;
  nomeTurma?: string;
  tipo: string;
  tipoDescricao: string;
  titulo: string;
  descricao?: string;
  dataPrevista?: string;
  horario?: string;
  sala?: string;
}

const PortalAlunoProgramacao: React.FC = () => {
  const [itens, setItens] = useState<ItemProgramacao[]>([]);
  const [erro, setErro] = useState('');

  useEffect(() => {
    HttpService.portalAlunoProgramacao()
      .then((r) => setItens(r.data))
      .catch((e) => setErro(extractApiMessage(e, 'Não foi possível carregar sua programação.')));
  }, []);

  const ordenados = useMemo(() => {
    return [...itens].sort((a, b) => {
      const da = a.dataPrevista || '9999-12-31';
      const db = b.dataPrevista || '9999-12-31';
      if (da !== db) return da.localeCompare(db);
      return (a.horario || '').localeCompare(b.horario || '');
    });
  }, [itens]);

  const proximos = ordenados.filter((i) => !i.dataPrevista || i.dataPrevista >= new Date().toISOString().slice(0, 10));

  return (
    <PageShell
      title="Minha programação"
      subtitle={COPY_UI.portalAluno.programacaoSubtitulo}
    >
      {erro && <p style={{ color: '#b91c1c' }}>{erro}</p>}
      {!erro && ordenados.length === 0 && (
        <div className="card">
          <p>{COPY_UI.portalAluno.programacaoVazio}</p>
        </div>
      )}
      {!erro && proximos.length > 0 && (
        <p className="field-hint" style={{ marginBottom: '0.75rem' }}>
          {proximos.length} {proximos.length === 1 ? 'atividade prevista' : 'atividades previstas'} a partir de hoje
        </p>
      )}
      <div className="portal-programacao-list">
        {ordenados.map((item) => (
          <article
            key={item.id}
            className={`portal-programacao-card portal-programacao-card--${item.tipo}`}
          >
            <p style={{ margin: 0 }}>
              <span className={`tipo-badge tipo-badge--${item.tipo}`}>{item.tipoDescricao || item.tipo}</span>
              {item.escopoLancamento === 'TURMA' && item.nomeTurma && (
                <span className="field-hint" style={{ marginLeft: '0.35rem' }}>· Turma: {item.nomeTurma}</span>
              )}
              <strong style={{ marginLeft: '0.35rem' }}>{item.titulo}</strong>
            </p>
            {(item.dataPrevista || item.horario || item.sala) && (
              <p className="field-hint" style={{ margin: '0.4rem 0 0' }}>
                {item.dataPrevista && new Date(`${item.dataPrevista}T12:00:00`).toLocaleDateString('pt-BR', {
                  weekday: 'long',
                  day: '2-digit',
                  month: 'long',
                })}
                {item.horario ? ` · ${item.horario}` : ''}
                {item.sala ? ` · ${item.sala}` : ''}
              </p>
            )}
            {item.descricao && <p style={{ marginTop: '0.5rem', marginBottom: 0 }}>{item.descricao}</p>}
          </article>
        ))}
      </div>
    </PageShell>
  );
};

export default PortalAlunoProgramacao;

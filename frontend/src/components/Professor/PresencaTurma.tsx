import { useCallback, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import HttpService from '../../services/HttpService';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { extractApiMessage } from '../../utils/apiError';
import './PresencaTurma.css';

type StatusPresenca = 'P' | 'F' | 'J' | 'A' | '';

interface AlunoLinha {
  nome: string;
  cpf: string;
  registros: Record<number, string>;
  totais: Record<string, number>;
  percentuais: Record<string, number>;
}

interface GradePresenca {
  turmaId: number;
  modalidade: string;
  sala?: string;
  horario?: string;
  ano: number;
  mes: number;
  diasComAula: number[];
  alunos: AlunoLinha[];
}

const CICLO: StatusPresenca[] = ['', 'P', 'F', 'J', 'A'];

const proximoStatus = (atual: string): StatusPresenca => {
  const idx = CICLO.indexOf((atual || '') as StatusPresenca);
  return CICLO[(idx + 1) % CICLO.length];
};

const PresencaTurma: React.FC = () => {
  const [params] = useSearchParams();
  const hoje = new Date();
  const [turmas, setTurmas] = useState<Array<{ id: number; modalidade: string; horario: string }>>([]);
  const [turmaId, setTurmaId] = useState(params.get('turma') || '');
  const [ano, setAno] = useState(hoje.getFullYear());
  const [mes, setMes] = useState(hoje.getMonth() + 1);
  const [grade, setGrade] = useState<GradePresenca | null>(null);
  const [alteracoes, setAlteracoes] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  useEffect(() => {
    HttpService.minhasTurmasProfessor().then((r) => setTurmas(r.data || [])).catch(() => setTurmas([]));
  }, []);

  const carregar = useCallback(() => {
    if (!turmaId) {
      setGrade(null);
      return;
    }
    setLoading(true);
    HttpService.presencaConsultar(turmaId, ano, mes)
      .then((r) => {
        setGrade(r.data);
        setAlteracoes({});
      })
      .catch(() => setGrade(null))
      .finally(() => setLoading(false));
  }, [turmaId, ano, mes]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const chave = (cpf: string, dia: number) => `${cpf}:${dia}`;

  const valorCelula = useCallback((aluno: AlunoLinha, dia: number) => {
    const k = chave(aluno.cpf, dia);
    if (k in alteracoes) return alteracoes[k];
    return aluno.registros[dia] || '';
  }, [alteracoes]);

  const alternarCelula = (aluno: AlunoLinha, dia: number) => {
    const atual = valorCelula(aluno, dia);
    const novo = proximoStatus(atual);
    setAlteracoes((prev) => ({ ...prev, [chave(aluno.cpf, dia)]: novo }));
  };

  const temAlteracoes = Object.keys(alteracoes).length > 0;

  const salvar = async () => {
    if (!turmaId || !grade) return;
    const registros = Object.entries(alteracoes)
      .filter(([, status]) => status)
      .map(([k, status]) => {
        const [alunoCpf, diaStr] = k.split(':');
        return { alunoCpf, dia: Number(diaStr), status };
      });
    if (registros.length === 0) return;
    setLoading(true);
    try {
      await HttpService.presencaSalvar(turmaId, { ano, mes, registros });
      setModal({ open: true, success: true, message: 'Presença salva com sucesso.' });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao salvar presença.') });
    } finally {
      setLoading(false);
    }
  };

  const baixarPdf = async () => {
    if (!turmaId) return;
    try {
      const { data } = await HttpService.presencaPdf(turmaId, ano, mes);
      const url = URL.createObjectURL(new Blob([data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.download = `presenca_${turmaId}_${ano}_${mes}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao gerar PDF.') });
    }
  };

  const totaisLinha = useMemo(() => {
    if (!grade) return {};
    const mapa: Record<string, Record<string, number>> = {};
    grade.alunos.forEach((a) => {
      const t = { P: 0, F: 0, J: 0, A: 0 };
      grade.diasComAula.forEach((dia) => {
        const v = valorCelula(a, dia);
        if (v === 'P' || v === 'F' || v === 'J' || v === 'A') t[v]++;
      });
      mapa[a.cpf] = t;
    });
    return mapa;
  }, [grade, valorCelula]);

  return (
    <PageShell title="Presença" subtitle="Planilha mensal de frequência por turma">
      <div className="card">
        <div className="presenca-toolbar">
          <div>
            <label>Turma</label>
            <select value={turmaId} onChange={(e) => setTurmaId(e.target.value)}>
              <option value="">—</option>
              {turmas.map((t) => (
                <option key={t.id} value={t.id}>{t.modalidade} ({t.horario})</option>
              ))}
            </select>
          </div>
          <div>
            <label>Mês</label>
            <input type="month" value={`${ano}-${String(mes).padStart(2, '0')}`}
              onChange={(e) => {
                const [y, m] = e.target.value.split('-');
                setAno(Number(y));
                setMes(Number(m));
              }} />
          </div>
          <button type="button" className="btn-primary" disabled={loading || !temAlteracoes} onClick={salvar}>
            Salvar alterações
          </button>
          <button type="button" className="btn-secondary" disabled={!turmaId} onClick={baixarPdf}>
            Gerar PDF
          </button>
        </div>

        {grade && (
          <>
            <p className="field-hint" style={{ marginTop: 0 }}>
              {grade.modalidade} · {grade.sala || '—'} · {grade.horario || '—'}
            </p>
            <div className="presenca-legenda">
              <span className="P">P — Presente</span>
              <span className="F">F — Falta</span>
              <span className="J">J — Justificada</span>
              <span className="A">A — Atraso</span>
            </div>
          </>
        )}

        {grade && grade.diasComAula.length === 0 && (
          <p>Nenhuma aula prevista neste mês para os dias cadastrados na turma.</p>
        )}

        {grade && grade.diasComAula.length > 0 && (
          <div className="presenca-grid-wrap">
            <table className="presenca-grid">
              <thead>
                <tr>
                  <th className="sticky-col">Aluno</th>
                  {grade.diasComAula.map((d) => <th key={d}>{d}</th>)}
                  <th>P</th><th>F</th><th>J</th><th>A</th>
                </tr>
              </thead>
              <tbody>
                {grade.alunos.map((a) => {
                  const t = totaisLinha[a.cpf] || { P: 0, F: 0, J: 0, A: 0 };
                  return (
                    <tr key={a.cpf}>
                      <td className="sticky-col">{a.nome}</td>
                      {grade.diasComAula.map((dia) => {
                        const val = valorCelula(a, dia);
                        return (
                          <td key={dia}>
                            <button
                              type="button"
                              className={`presenca-celula presenca-celula--${val || 'vazia'}`}
                              onClick={() => alternarCelula(a, dia)}
                              aria-label={`${a.nome}, dia ${dia}, ${val || 'sem registro'}`}
                            >
                              {val || '·'}
                            </button>
                          </td>
                        );
                      })}
                      <td>{t.P}</td><td>{t.F}</td><td>{t.J}</td><td>{t.A}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {turmaId && !grade && !loading && <p>Selecione turma e mês para carregar a planilha.</p>}
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

export default PresencaTurma;

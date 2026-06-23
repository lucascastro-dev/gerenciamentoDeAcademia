import { useCallback, useEffect, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { FolhaPontoUtil } from '../../utils/folhaPonto';
import './Colaborador.css';

const MESES = [
  { v: 1, l: 'Janeiro' }, { v: 2, l: 'Fevereiro' }, { v: 3, l: 'Março' },
  { v: 4, l: 'Abril' }, { v: 5, l: 'Maio' }, { v: 6, l: 'Junho' },
  { v: 7, l: 'Julho' }, { v: 8, l: 'Agosto' }, { v: 9, l: 'Setembro' },
  { v: 10, l: 'Outubro' }, { v: 11, l: 'Novembro' }, { v: 12, l: 'Dezembro' },
];

const MeuPonto: React.FC = () => {
  const hoje = new Date();
  const [mes, setMes] = useState(hoje.getMonth() + 1);
  const [ano, setAno] = useState(hoje.getFullYear());
  const [statusHoje, setStatusHoje] = useState<{
    proximaAcao: string;
    horaEntrada?: string;
    horaSaida?: string;
    mensagem: string;
  } | null>(null);
  const [resumo, setResumo] = useState<{
    registros: Array<{
      data: string;
      horaEntrada?: string;
      horaSaida?: string;
      horasFormatadas: string;
      situacao: string;
    }>;
    totalHorasFormatadas: string;
    diasComRegistroCompleto: number;
  } | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregar = useCallback(() => {
    setCarregando(true);
    Promise.all([
      HttpService.pontoStatusHoje(),
      HttpService.pontoMeuMes(mes, ano),
    ])
      .then(([statusRes, mesRes]) => {
        setStatusHoje(statusRes.data);
        setResumo(mesRes.data);
      })
      .catch(() => {
        setStatusHoje(null);
        setResumo(null);
      })
      .finally(() => setCarregando(false));
  }, [mes, ano]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const marcar = async () => {
    if (!statusHoje || statusHoje.proximaAcao === 'COMPLETO') return;
    try {
      const r = await HttpService.pontoMarcar();
      setStatusHoje(r.data);
      setModal({
        open: true,
        success: true,
        message: r.data.proximaAcao === 'SAIDA'
          ? 'Entrada registrada com sucesso.'
          : 'Saída registrada com sucesso.',
      });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao registrar ponto.') });
    }
  };

  const labelBotao = statusHoje?.proximaAcao === 'SAIDA'
    ? 'Registrar saída'
    : statusHoje?.proximaAcao === 'ENTRADA'
      ? 'Registrar entrada'
      : 'Dia concluído';

  return (
    <PageShell showBack={false}>
      <div className="colab-page colab-ponto">
        <div className="colab-ponto__acao card">
          <div>
            <h3 className="colab-ponto__titulo">Marcação do dia</h3>
            <p className="field-hint">{statusHoje?.mensagem || 'Carregando status...'}</p>
            {statusHoje?.horaEntrada && (
              <p className="colab-ponto__horario">
                Entrada: {FolhaPontoUtil.formatarDataHora(statusHoje.horaEntrada)}
                {statusHoje.horaSaida && ` · Saída: ${FolhaPontoUtil.formatarDataHora(statusHoje.horaSaida)}`}
              </p>
            )}
          </div>
          <button
            type="button"
            className="btn-primary colab-ponto__btn"
            disabled={!statusHoje || statusHoje.proximaAcao === 'COMPLETO' || carregando}
            onClick={marcar}
          >
            {labelBotao}
          </button>
        </div>

        <div className="colab-toolbar card">
          <div className="colab-toolbar__field">
            <label htmlFor="ponto-mes">Competência</label>
            <div className="colab-competencia">
              <select id="ponto-mes" value={mes} onChange={(e) => setMes(Number(e.target.value))}>
                {MESES.map((m) => (
                  <option key={m.v} value={m.v}>{m.l}</option>
                ))}
              </select>
              <select id="ponto-ano" value={ano} onChange={(e) => setAno(Number(e.target.value))}>
                {[hoje.getFullYear(), hoje.getFullYear() - 1].map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>
          </div>
          {resumo && (
            <div className="colab-ponto__total">
              <strong>{resumo.totalHorasFormatadas}</strong>
              <span>{resumo.diasComRegistroCompleto} dias completos no mês</span>
            </div>
          )}
        </div>

        <div className="card table-wrap">
          <table className="audit-table colab-ponto__tabela">
            <thead>
              <tr>
                <th>Data</th>
                <th>Entrada</th>
                <th>Saída</th>
                <th>Total dia</th>
                <th>Situação</th>
              </tr>
            </thead>
            <tbody>
              {!resumo?.registros.length && !carregando && (
                <tr><td colSpan={5}>Nenhum registro neste mês.</td></tr>
              )}
              {resumo?.registros.map((r) => (
                <tr key={r.data}>
                  <td>{FolhaPontoUtil.formatarData(r.data)}</td>
                  <td>{FolhaPontoUtil.formatarHora(r.horaEntrada)}</td>
                  <td>{FolhaPontoUtil.formatarHora(r.horaSaida)}</td>
                  <td>{r.horasFormatadas}</td>
                  <td>{r.situacao}</td>
                </tr>
              ))}
            </tbody>
            {resumo && resumo.registros.length > 0 && (
              <tfoot>
                <tr className="colab-ponto__footer">
                  <td colSpan={3}><strong>Total do mês</strong></td>
                  <td colSpan={2}><strong>{resumo.totalHorasFormatadas}</strong></td>
                </tr>
              </tfoot>
            )}
          </table>
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

export default MeuPonto;

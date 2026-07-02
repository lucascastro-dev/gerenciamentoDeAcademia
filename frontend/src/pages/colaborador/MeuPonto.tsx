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
  const [ajusteAberto, setAjusteAberto] = useState(false);
  const [ajusteData, setAjusteData] = useState('');
  const [ajusteEntrada, setAjusteEntrada] = useState('');
  const [ajusteSaida, setAjusteSaida] = useState('');
  const [ajusteJustificativa, setAjusteJustificativa] = useState('');
  const [meusAjustes, setMeusAjustes] = useState<Array<{
    id: number;
    dataRegistro: string;
    status: string;
    justificativa: string;
    observacaoGestor?: string;
  }>>([]);

  const carregar = useCallback(() => {
    setCarregando(true);
    Promise.all([
      HttpService.pontoStatusHoje(),
      HttpService.pontoMeuMes(mes, ano),
      HttpService.pontoMeusAjustes(),
    ])
      .then(([statusRes, mesRes, ajustesRes]) => {
        setStatusHoje(statusRes.data);
        setResumo(mesRes.data);
        setMeusAjustes(ajustesRes.data || []);
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

  const solicitarAjuste = async () => {
    if (!ajusteData || !ajusteJustificativa.trim()) {
      setModal({ open: true, success: false, message: 'Informe a data e a justificativa.' });
      return;
    }
    if (!ajusteEntrada && !ajusteSaida) {
      setModal({ open: true, success: false, message: 'Informe pelo menos um horário a ajustar.' });
      return;
    }
    try {
      await HttpService.pontoSolicitarAjuste({
        dataRegistro: ajusteData,
        horaEntradaProposta: ajusteEntrada || undefined,
        horaSaidaProposta: ajusteSaida || undefined,
        justificativa: ajusteJustificativa.trim(),
      });
      setAjusteAberto(false);
      setAjusteData('');
      setAjusteEntrada('');
      setAjusteSaida('');
      setAjusteJustificativa('');
      setModal({ open: true, success: true, message: 'Solicitação enviada para aprovação do RH/Administrador.' });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao solicitar ajuste.') });
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

        <div className="card" style={{ marginBottom: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
            <h3 className="colab-ponto__titulo" style={{ margin: 0 }}>Ajuste de horário</h3>
            <button type="button" className="btn-secondary" onClick={() => setAjusteAberto((v) => !v)}>
              {ajusteAberto ? 'Cancelar' : 'Solicitar ajuste'}
            </button>
          </div>
          <p className="field-hint">Correções de entrada/saída precisam de aprovação do RH ou Administrador.</p>
          {ajusteAberto && (
            <div className="form-grid" style={{ marginTop: '0.75rem' }}>
              <div><label>Data</label><input type="date" value={ajusteData} onChange={(e) => setAjusteData(e.target.value)} /></div>
              <div><label>Nova entrada</label><input type="time" value={ajusteEntrada} onChange={(e) => setAjusteEntrada(e.target.value)} /></div>
              <div><label>Nova saída</label><input type="time" value={ajusteSaida} onChange={(e) => setAjusteSaida(e.target.value)} /></div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label>Justificativa</label>
                <textarea rows={3} value={ajusteJustificativa} onChange={(e) => setAjusteJustificativa(e.target.value)} />
              </div>
              <div><button type="button" className="btn-primary" onClick={solicitarAjuste}>Enviar solicitação</button></div>
            </div>
          )}
          {meusAjustes.length > 0 && (
            <ul style={{ margin: '1rem 0 0', paddingLeft: '1.2rem' }}>
              {meusAjustes.slice(0, 5).map((a) => (
                <li key={a.id}>
                  {FolhaPontoUtil.formatarData(a.dataRegistro)} — {a.status} — {a.justificativa}
                </li>
              ))}
            </ul>
          )}
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

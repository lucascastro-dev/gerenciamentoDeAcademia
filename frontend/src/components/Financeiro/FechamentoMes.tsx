import { useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import './FinanceiroOperacional.css';

interface EtapaFechamento {
  id: string;
  titulo: string;
  descricao: string;
  concluida: boolean;
}

const ETAPAS_INICIAIS: EtapaFechamento[] = [
  { id: 'mensalidades', titulo: 'Mensalidades conferidas', descricao: 'Baixas e inadimplência revisadas', concluida: false },
  { id: 'folha', titulo: 'Folha de pagamento', descricao: 'Proventos processados e holerites publicados', concluida: false },
  { id: 'conciliacao', titulo: 'Conciliação bancária', descricao: 'Extrato x recebimentos do mês', concluida: false },
  { id: 'dre', titulo: 'Resumo do mês (DRE simplificado)', descricao: 'Receitas, despesas e saldo consolidado', concluida: false },
];

const FechamentoMes: React.FC = () => {
  const hoje = new Date();
  const [mes, setMes] = useState(String(hoje.getMonth()));
  const [ano, setAno] = useState(String(hoje.getFullYear()));
  const [etapas, setEtapas] = useState(ETAPAS_INICIAIS);
  const [fechado, setFechado] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const MESES = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
  ];

  const progresso = Math.round((etapas.filter((e) => e.concluida).length / etapas.length) * 100);

  const toggleEtapa = (id: string) => {
    if (fechado) return;
    setEtapas((lista) => lista.map((e) => (e.id === id ? { ...e, concluida: !e.concluida } : e)));
  };

  const fecharMes = () => {
    if (progresso < 100) {
      setModal({ open: true, success: false, message: 'Conclua todas as etapas antes de fechar o mês.' });
      return;
    }
    setFechado(true);
    setModal({
      open: true,
      success: true,
      message: `Mês ${MESES[Number(mes)]}/${ano} fechado. Alterações retroativas exigirão reabertura.`,
    });
  };

  return (
    <PageShell showBack={false}>
      <div className="fin-op">
        <div className="fin-op__toolbar card">
          <div>
            <label htmlFor="fechamento-mes">Período</label>
            <div className="fin-op__competencia">
              <select id="fechamento-mes" value={mes} onChange={(e) => setMes(e.target.value)} disabled={fechado}>
                {MESES.map((m, i) => (
                  <option key={m} value={String(i)}>{m}</option>
                ))}
              </select>
              <select id="fechamento-ano" value={ano} onChange={(e) => setAno(e.target.value)} disabled={fechado}>
                {[hoje.getFullYear(), hoje.getFullYear() - 1].map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="fin-op__progresso">
            <span>{progresso}% concluído</span>
            <div className="fin-op__progresso-track">
              <div style={{ width: `${progresso}%` }} />
            </div>
          </div>
        </div>

        <div className="fin-op__etapas">
          {etapas.map((etapa) => (
            <button
              key={etapa.id}
              type="button"
              className={`fin-op__etapa card${etapa.concluida ? ' fin-op__etapa--ok' : ''}`}
              onClick={() => toggleEtapa(etapa.id)}
              disabled={fechado}
            >
              <span className="fin-op__etapa-check">{etapa.concluida ? '✓' : ''}</span>
              <div>
                <strong>{etapa.titulo}</strong>
                <p className="field-hint">{etapa.descricao}</p>
              </div>
            </button>
          ))}
        </div>

        <div className="fin-op__actions">
          <button type="button" className="btn-primary" onClick={fecharMes} disabled={fechado}>
            {fechado ? 'Mês fechado' : 'Fechar mês'}
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

export default FechamentoMes;

import { useCallback, useEffect, useMemo, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import './FinanceiroOperacional.css';

interface ColaboradorFolha {
  cpf: string;
  nome: string;
  cargo: string;
  salario: number;
  status: string;
  reciboPublicado: boolean;
  horasTrabalhadas?: string;
  diasTrabalhados?: number;
}

const MESES = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

const FolhaPagamento: React.FC = () => {
  const hoje = new Date();
  const [mes, setMes] = useState(String(hoje.getMonth() + 1));
  const [ano, setAno] = useState(String(hoje.getFullYear()));
  const [folha, setFolha] = useState<ColaboradorFolha[]>([]);
  const [pontoRh, setPontoRh] = useState<{ pontoConferidoRh: boolean; integradoFinanceiro: boolean } | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregar = useCallback(() => {
    setCarregando(true);
    Promise.all([
      HttpService.folhaPagamentoColaboradores(Number(mes), Number(ano)),
      HttpService.financeiroPontoStatusIntegracao(Number(mes), Number(ano)),
    ])
      .then(([folhaRes, pontoRes]) => {
        setFolha((folhaRes.data || []).map((c) => ({
          cpf: c.cpf,
          nome: c.nome,
          cargo: c.cargo,
          salario: Number(c.salarioBase),
          status: c.statusPagamento,
          reciboPublicado: c.reciboPublicado,
          horasTrabalhadas: c.horasTrabalhadasFormatadas || '0h',
          diasTrabalhados: c.diasTrabalhados ?? 0,
        })));
        setPontoRh(pontoRes.data);
      })
      .catch(() => {
        setFolha([]);
        setPontoRh(null);
      })
      .finally(() => setCarregando(false));
  }, [mes, ano]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const totais = useMemo(() => {
    const bruto = folha.reduce((s, c) => s + c.salario, 0);
    const encargos = bruto * 0.28;
    return { bruto, encargos, liquido: bruto - encargos * 0.15, colaboradores: folha.length };
  }, [folha]);

  const fmt = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  const integrarPonto = async () => {
    try {
      const r = await HttpService.financeiroPontoIntegrar(Number(mes), Number(ano));
      setModal({ open: true, success: true, message: r.data.message });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao integrar ponto.') });
    }
  };

  const confirmarPagamento = async (colaborador: ColaboradorFolha) => {
    if (!pontoRh?.integradoFinanceiro) {
      setModal({
        open: true,
        success: false,
        message: 'Integre a folha de ponto do mês antes de confirmar pagamentos.',
      });
      return;
    }
    if (colaborador.status !== 'Processado') {
      setModal({
        open: true,
        success: false,
        message: 'O holerite precisa estar publicado pelo RH (status Processado) antes do pagamento.',
      });
      return;
    }
    try {
      const r = await HttpService.folhaConfirmarPagamento({
        cpfColaborador: colaborador.cpf,
        mesCompetencia: Number(mes),
        anoCompetencia: Number(ano),
        valorBruto: colaborador.salario,
        valorLiquido: colaborador.salario * 0.85,
      });
      setModal({
        open: true,
        success: true,
        message: r.data.message || 'Recibo publicado para o colaborador.',
      });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao confirmar pagamento.') });
    }
  };

  return (
    <PageShell showBack={false} title="Folha de pagamento (legado)">
      <div className="fin-op">
        <div className="card" style={{ marginBottom: '1rem', borderLeft: '4px solid var(--brand-energy)' }}>
          <p className="field-hint" style={{ margin: 0 }}>
            Módulo legado em manutenção. Para holerite e recibo, use{' '}
            <strong>Gestão de equipe → Anexar holerite / recibo (PDF)</strong> com documentos do seu ERP.
          </p>
        </div>
        <div className="fin-op__toolbar card">
          <div>
            <label htmlFor="folha-mes">Competência</label>
            <div className="fin-op__competencia">
              <select id="folha-mes" value={mes} onChange={(e) => setMes(e.target.value)}>
                {MESES.map((m, i) => (
                  <option key={m} value={String(i + 1)}>{m}</option>
                ))}
              </select>
              <select id="folha-ano" value={ano} onChange={(e) => setAno(e.target.value)}>
                {[hoje.getFullYear(), hoje.getFullYear() - 1].map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="fin-op__ponto-badges">
            {pontoRh?.pontoConferidoRh
              ? <span className="fin-op__badge fin-op__badge--pago">Ponto conferido RH</span>
              : <span className="fin-op__badge fin-op__badge--pendente">Aguardando RH</span>}
            {pontoRh?.integradoFinanceiro && (
              <span className="fin-op__badge fin-op__badge--pago">Ponto integrado</span>
            )}
          </div>
        </div>

        {pontoRh?.pontoConferidoRh && !pontoRh.integradoFinanceiro && (
          <div className="fin-op__actions">
            <button type="button" className="btn-primary" onClick={integrarPonto}>
              Integrar folha de ponto
            </button>
          </div>
        )}

        <div className="fin-op__kpis">
          <div className="fin-op__kpi card"><strong>{totais.colaboradores}</strong><span>Colaboradores</span></div>
          <div className="fin-op__kpi card"><strong>{fmt(totais.bruto)}</strong><span>Proventos</span></div>
          <div className="fin-op__kpi card"><strong>{fmt(totais.encargos)}</strong><span>Encargos estimados</span></div>
          <div className="fin-op__kpi card"><strong>{fmt(totais.liquido)}</strong><span>Líquido estimado</span></div>
        </div>

        <div className="card table-wrap">
          {carregando && <p className="field-hint">Carregando colaboradores...</p>}
          <table className="audit-table">
            <thead>
              <tr>
                <th>Colaborador</th>
                <th>Cargo</th>
                <th>Horas (ponto)</th>
                <th>Salário base</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {folha.length === 0 && !carregando && (
                <tr><td colSpan={6}>Nenhum colaborador ativo na instituição.</td></tr>
              )}
              {folha.map((c) => (
                <tr key={c.cpf}>
                  <td>{c.nome}</td>
                  <td>{c.cargo}</td>
                  <td>{c.horasTrabalhadas} ({c.diasTrabalhados}d)</td>
                  <td>{fmt(c.salario)}</td>
                  <td>
                    <span className={`fin-op__badge fin-op__badge--${c.status === 'Pago' ? 'pago' : 'pendente'}`}>
                      {c.status}
                    </span>
                  </td>
                  <td>
                    {c.status === 'Processado' && (
                      <button type="button" className="btn-secondary" onClick={() => confirmarPagamento(c)}>
                        Confirmar pagamento
                      </button>
                    )}
                    {c.reciboPublicado && <span className="field-hint">Recibo publicado</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="field-hint">
          Fluxo: Colaborador marca ponto → RH confere → Financeiro integra → RH publica holerite → Financeiro confirma pagamento → Recibo em Meu holerite.
        </p>
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

export default FolhaPagamento;

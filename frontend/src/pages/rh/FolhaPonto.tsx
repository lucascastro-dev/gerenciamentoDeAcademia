import { useCallback, useEffect, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import { carregarSessao, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { FolhaPontoUtil } from '../../utils/folhaPonto';
import '../../components/Financeiro/FinanceiroOperacional.css';
import './Rh.css';

const MESES = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

interface ColaboradorPonto {
  cpf: string;
  nome: string;
  cargo: string;
  diasTrabalhados: number;
  horasFormatadas: string;
  possuiRegistroAberto: boolean;
}

const FolhaPonto: React.FC = () => {
  const sessao = carregarSessao();
  const podeConferir = possuiPermissao(sessao, 'rh:fechamento-mensal');
  const hoje = new Date();
  const [mes, setMes] = useState(String(hoje.getMonth() + 1));
  const [ano, setAno] = useState(String(hoje.getFullYear()));
  const [colaboradores, setColaboradores] = useState<ColaboradorPonto[]>([]);
  const [statusIntegracao, setStatusIntegracao] = useState<{
    pontoConferidoRh: boolean;
    integradoFinanceiro: boolean;
    colaboradoresComRegistro: number;
  } | null>(null);
  const [detalhe, setDetalhe] = useState<{ nome: string; registros: Array<{
    data: string; horaEntrada?: string; horaSaida?: string; horasFormatadas: string; situacao: string;
  }>; total: string } | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const [ajustesPendentes, setAjustesPendentes] = useState<Array<{
    id: number;
    nomeColaborador: string;
    dataRegistro: string;
    horaEntradaAtual?: string;
    horaSaidaAtual?: string;
    horaEntradaProposta?: string;
    horaSaidaProposta?: string;
    justificativa: string;
    status: string;
  }>>([]);

  const carregar = useCallback(() => {
    setCarregando(true);
    Promise.all([
      HttpService.rhFolhaPontoColaboradores(Number(mes), Number(ano)),
      HttpService.rhFolhaPontoStatusIntegracao(Number(mes), Number(ano)),
      podeConferir ? HttpService.rhPontoAjustesListar('PENDENTE') : Promise.resolve({ data: [] }),
    ])
      .then(([colabRes, statusRes, ajustesRes]) => {
        setColaboradores(colabRes.data || []);
        setStatusIntegracao(statusRes.data);
        setAjustesPendentes(ajustesRes.data || []);
      })
      .catch(() => {
        setColaboradores([]);
        setStatusIntegracao(null);
      })
      .finally(() => setCarregando(false));
  }, [mes, ano, podeConferir]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const decidirAjuste = async (id: number, status: 'APROVADO' | 'REJEITADO') => {
    try {
      await HttpService.rhPontoAjusteDecidir(id, { status });
      setModal({
        open: true,
        success: true,
        message: status === 'APROVADO' ? 'Ajuste aprovado.' : 'Ajuste rejeitado.',
      });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao decidir ajuste.') });
    }
  };

  const conferir = async () => {
    try {
      const r = await HttpService.rhFolhaPontoConferir(Number(mes), Number(ano));
      setModal({ open: true, success: true, message: r.data.message });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao conferir ponto.') });
    }
  };

  const reabrir = async () => {
    if (!window.confirm('Reabrir a conferência desta competência? Colaboradores voltarão a poder marcar ponto no mês.')) {
      return;
    }
    try {
      const r = await HttpService.rhFolhaPontoReabrir(Number(mes), Number(ano));
      setModal({ open: true, success: true, message: r.data.message });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao reabrir conferência.') });
    }
  };

  const abrirDetalhe = async (c: ColaboradorPonto) => {
    try {
      const r = await HttpService.rhFolhaPontoDetalhe(c.cpf, Number(mes), Number(ano));
      setDetalhe({
        nome: c.nome,
        registros: r.data.registros || [],
        total: r.data.totalHorasFormatadas,
      });
    } catch {
      setModal({ open: true, success: false, message: 'Não foi possível carregar o detalhe.' });
    }
  };

  return (
    <PageShell showBack={false}>
      <div className="rh-page">
        {podeConferir && ajustesPendentes.length > 0 && (
          <div className="card" style={{ marginBottom: '1rem' }}>
            <h3 style={{ marginTop: 0 }}>Ajustes de ponto pendentes</h3>
            <div className="table-wrap">
              <table className="audit-table">
                <thead>
                  <tr>
                    <th>Colaborador</th>
                    <th>Data</th>
                    <th>Proposta</th>
                    <th>Justificativa</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {ajustesPendentes.map((a) => (
                    <tr key={a.id}>
                      <td>{a.nomeColaborador}</td>
                      <td>{FolhaPontoUtil.formatarData(a.dataRegistro)}</td>
                      <td>
                        {FolhaPontoUtil.formatarHora(a.horaEntradaProposta)} → {FolhaPontoUtil.formatarHora(a.horaSaidaProposta)}
                      </td>
                      <td>{a.justificativa}</td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <button type="button" className="btn-primary btn-sm" onClick={() => decidirAjuste(a.id, 'APROVADO')}>Aprovar</button>
                        {' '}
                        <button type="button" className="btn-secondary btn-sm" onClick={() => decidirAjuste(a.id, 'REJEITADO')}>Rejeitar</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        <div className="fin-op__toolbar card">
          <div>
            <label htmlFor="ponto-mes">Competência</label>
            <div className="fin-op__competencia">
              <select id="ponto-mes" value={mes} onChange={(e) => setMes(e.target.value)}>
                {MESES.map((m, i) => (
                  <option key={m} value={String(i + 1)}>{m}</option>
                ))}
              </select>
              <select id="ponto-ano" value={ano} onChange={(e) => setAno(e.target.value)}>
                {[hoje.getFullYear(), hoje.getFullYear() - 1].map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="rh-ponto__status">
            {statusIntegracao?.pontoConferidoRh && (
              <span className="fin-op__badge fin-op__badge--pago">Conferido RH</span>
            )}
            {statusIntegracao?.integradoFinanceiro && (
              <span className="fin-op__badge fin-op__badge--pago">Integrado Financeiro</span>
            )}
          </div>
        </div>

        {podeConferir && (
          <div className="fin-op__actions">
            <button
              type="button"
              className="btn-primary"
              disabled={statusIntegracao?.pontoConferidoRh || carregando}
              onClick={conferir}
            >
              Conferir folha de ponto
            </button>
            {statusIntegracao?.pontoConferidoRh && !statusIntegracao?.integradoFinanceiro && (
              <button
                type="button"
                className="btn-secondary"
                disabled={carregando}
                onClick={reabrir}
              >
                Reabrir conferência
              </button>
            )}
            <p className="field-hint">
              Conferir apenas meses já encerrados. Após conferir, os colaboradores marcam ponto no mês seguinte.
              Registros em aberto bloqueiam a conferência.
            </p>
          </div>
        )}

        <div className="card table-wrap">
          {carregando && <p className="field-hint">Carregando...</p>}
          <table className="audit-table">
            <thead>
              <tr>
                <th>Colaborador</th>
                <th>Função</th>
                <th>Dias</th>
                <th>Horas</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {colaboradores.length === 0 && !carregando && (
                <tr><td colSpan={5}>Nenhum colaborador ativo.</td></tr>
              )}
              {colaboradores.map((c) => (
                <tr key={c.cpf}>
                  <td>{c.nome}{c.possuiRegistroAberto && ' ⚠'}</td>
                  <td>{c.cargo}</td>
                  <td>{c.diasTrabalhados}</td>
                  <td>{c.horasFormatadas}</td>
                  <td>
                    <button type="button" className="btn-secondary" onClick={() => abrirDetalhe(c)}>
                      Detalhe
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {detalhe && (
        <div className="colab-modal-backdrop" role="presentation" onClick={() => setDetalhe(null)}>
          <div className="card colab-modal colab-doc-view" role="dialog" onClick={(e) => e.stopPropagation()}>
            <h3>{detalhe.nome}</h3>
            <table className="audit-table">
              <thead>
                <tr><th>Data</th><th>Entrada</th><th>Saída</th><th>Total</th></tr>
              </thead>
              <tbody>
                {detalhe.registros.map((r) => (
                  <tr key={r.data}>
                    <td>{FolhaPontoUtil.formatarData(r.data)}</td>
                    <td>{FolhaPontoUtil.formatarHora(r.horaEntrada)}</td>
                    <td>{FolhaPontoUtil.formatarHora(r.horaSaida)}</td>
                    <td>{r.horasFormatadas}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr><td colSpan={3}><strong>Total</strong></td><td><strong>{detalhe.total}</strong></td></tr>
              </tfoot>
            </table>
            <button type="button" className="btn-secondary" onClick={() => setDetalhe(null)}>Fechar</button>
          </div>
        </div>
      )}

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default FolhaPonto;

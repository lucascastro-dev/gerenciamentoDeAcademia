import { useCallback, useEffect, useState } from 'react';
import { COPY_UI } from '../../constants/copy';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import type { CobrancaExternaResposta } from '../../services/api/integracoesApi';
import { extractApiMessage } from '../../utils/apiError';
import '../../theme/portal-aluno.css';

interface ResumoMensalidade {
  valorMensalidade?: number;
  diaVencimento?: number;
  inadimplente?: boolean;
  dataUltimoPagamento?: string;
}

interface MensalidadeItem {
  mes: number;
  ano: number;
  dataVencimento?: string;
  dataPagamento?: string;
  status: string;
  statusDescricao: string;
  valor?: number;
  cobrancaId?: number;
  podeGerarCobranca: boolean;
}

type FormaPagamento = 'PIX' | 'CREDIT_CARD';

const MESES = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

const cartaoVazio = () => ({
  holderName: '',
  number: '',
  expiryMonth: '',
  expiryYear: '',
  ccv: '',
  holderEmail: '',
  holderCpf: '',
  holderPostalCode: '',
  holderAddressNumber: '',
  holderPhone: '',
});

const PortalAlunoMensalidades: React.FC = () => {
  const anoAtual = new Date().getFullYear();
  const [ano, setAno] = useState(anoAtual);
  const [resumo, setResumo] = useState<ResumoMensalidade | null>(null);
  const [itens, setItens] = useState<MensalidadeItem[]>([]);
  const [infoPagamento, setInfoPagamento] = useState('');
  const [modoLocal, setModoLocal] = useState(true);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });
  const [cobrancaModal, setCobrancaModal] = useState<{
    open: boolean;
    item: MensalidadeItem | null;
    forma: FormaPagamento | null;
    modo: 'novo' | 'visualizar';
  }>({ open: false, item: null, forma: null, modo: 'novo' });
  const [cobranca, setCobranca] = useState<CobrancaExternaResposta | null>(null);
  const [cartao, setCartao] = useState(cartaoVazio());
  const [pixCopiado, setPixCopiado] = useState(false);

  const carregar = useCallback(() => {
    HttpService.portalAlunoMensalidade().then((r) => setResumo(r.data)).catch((e) => {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    });
    HttpService.portalHistoricoMensalidades(ano).then((r) => setItens(r.data || [])).catch(() => setItens([]));
    HttpService.portalPagamentoInfo().then((r) => {
      setInfoPagamento(r.data.message);
      setModoLocal(!!r.data.modoLocal);
    }).catch(() => {});
  }, [ano]);

  useEffect(() => { carregar(); }, [carregar]);

  const fecharModal = () => {
    setCobrancaModal({ open: false, item: null, forma: null, modo: 'novo' });
    setCobranca(null);
    setCartao(cartaoVazio());
    setPixCopiado(false);
  };

  const abrirNovaCobranca = (item: MensalidadeItem) => {
    setCobranca(null);
    setCartao(cartaoVazio());
    setPixCopiado(false);
    setCobrancaModal({ open: true, item, forma: null, modo: 'novo' });
  };

  const verPagamento = async (item: MensalidadeItem) => {
    if (!item.cobrancaId) return;
    setLoading(true);
    try {
      const r = await HttpService.consultarCobrancaMensalidade(item.cobrancaId);
      const dados = r.data;
      setCobranca(dados);
      setPixCopiado(false);
      setCobrancaModal({
        open: true,
        item,
        forma: dados.billingType === 'CREDIT_CARD' ? 'CREDIT_CARD' : 'PIX',
        modo: 'visualizar',
      });
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    } finally {
      setLoading(false);
    }
  };

  const refazerCobranca = (item: MensalidadeItem) => {
    abrirNovaCobranca(item);
  };

  const gerarCobranca = async () => {
    if (!cobrancaModal.item || !cobrancaModal.forma) return;
    setLoading(true);
    try {
      const payload = cobrancaModal.forma === 'CREDIT_CARD'
        ? { formaPagamento: 'CREDIT_CARD', cartao }
        : { formaPagamento: 'PIX' };
      const r = await HttpService.criarCobrancaMensalidade(
        cobrancaModal.item.mes,
        cobrancaModal.item.ano,
        payload,
      );
      setCobranca(r.data);
      carregar();
      setModal({
        open: true,
        success: true,
        message: cobrancaModal.forma === 'PIX'
          ? 'Cobrança PIX gerada. Escaneie o QR Code ou copie o código.'
          : 'Pagamento com cartão enviado para processamento.',
      });
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    } finally {
      setLoading(false);
    }
  };

  const simularPagamento = async () => {
    if (!cobranca?.id) return;
    setLoading(true);
    try {
      await HttpService.simularPagamentoMensalidade(cobranca.id);
      fecharModal();
      carregar();
      setModal({ open: true, success: true, message: 'Pagamento simulado com sucesso.' });
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    } finally {
      setLoading(false);
    }
  };

  const copiarPix = async () => {
    if (!cobranca?.pixCopiaCola) return;
    try {
      await navigator.clipboard.writeText(cobranca.pixCopiaCola);
      setPixCopiado(true);
    } catch {
      setModal({ open: true, success: false, message: 'Não foi possível copiar o código PIX.' });
    }
  };

  const formatarData = (iso?: string) => {
    if (!iso) return '—';
    const [y, m, d] = iso.split('T')[0].split('-');
    return y && m && d ? `${d}/${m}/${y}` : iso;
  };

  const tituloModal = () => {
    if (!cobrancaModal.item) return 'Pagamento';
    const mesAno = `${MESES[cobrancaModal.item.mes - 1]}/${cobrancaModal.item.ano}`;
    if (cobranca) return `Pagamento — ${mesAno}`;
    if (cobrancaModal.modo === 'visualizar') return `Cobrança pendente — ${mesAno}`;
    return `Gerar cobrança — ${mesAno}`;
  };

  const exibirPix = cobranca && (cobranca.billingType === 'PIX' || cobranca.pixQrCode || cobranca.pixCopiaCola);
  const exibirCartaoProcessado = cobranca && cobrancaModal.forma === 'CREDIT_CARD' && !exibirPix;

  return (
    <PageShell title="Mensalidades" subtitle={COPY_UI.portalAluno.mensalidadesSubtitulo}>
      {resumo && (
        <div className="card portal-aluno-resumo" style={{ marginBottom: '1rem' }}>
          <p><strong>Valor mensal:</strong> R$ {resumo.valorMensalidade?.toFixed(2)}</p>
          <p><strong>Vencimento:</strong> dia {resumo.diaVencimento}</p>
          <p><strong>Situação atual:</strong> {resumo.inadimplente ? 'Em aberto / atrasada' : 'Em dia no mês atual'}</p>
        </div>
      )}

      <div className="card" style={{ marginBottom: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
          <h3 style={{ margin: 0 }}>Histórico por ano</h3>
          <select value={ano} onChange={(e) => setAno(Number(e.target.value))} aria-label="Ano">
            {[anoAtual, anoAtual - 1, anoAtual - 2].map((a) => (
              <option key={a} value={a}>{a}</option>
            ))}
          </select>
        </div>
        <p className="field-hint" style={{ marginTop: '0.5rem' }}>{infoPagamento}</p>

        <div className="table-wrap" style={{ marginTop: '1rem' }}>
          <table className="audit-table">
            <thead>
              <tr>
                <th>Mês</th>
                <th>Vencimento</th>
                <th>Pagamento</th>
                <th>Status</th>
                <th>Valor</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {itens.length === 0 && (
                <tr><td colSpan={6}>Nenhuma mensalidade encontrada.</td></tr>
              )}
              {itens.map((item) => (
                <tr key={`${item.ano}-${item.mes}`}>
                  <td>{MESES[item.mes - 1]}</td>
                  <td>{formatarData(item.dataVencimento)}</td>
                  <td>{formatarData(item.dataPagamento)}</td>
                  <td>
                    <span className={`cobranca-status cobranca-status--${item.status.toLowerCase()}`}>
                      {item.statusDescricao}
                    </span>
                  </td>
                  <td>{item.valor != null ? `R$ ${Number(item.valor).toFixed(2)}` : '—'}</td>
                  <td>
                    <div className="cobranca-acoes">
                      {item.podeGerarCobranca && (
                        <button type="button" className="btn-primary btn-sm" onClick={() => abrirNovaCobranca(item)}>
                          Gerar cobrança
                        </button>
                      )}
                      {item.status !== 'PAGO' && item.cobrancaId && (
                        <>
                          <button type="button" className="btn-secondary btn-sm" onClick={() => verPagamento(item)}>
                            Ver pagamento
                          </button>
                          <button type="button" className="btn-link btn-sm" onClick={() => refazerCobranca(item)}>
                            Refazer
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {cobrancaModal.open && (
        <div className="modal-overlay cobranca-modal-overlay" role="dialog" aria-modal="true" onClick={fecharModal}>
          <div
            className="cobranca-modal"
            onClick={(e) => e.stopPropagation()}
            aria-labelledby="cobranca-modal-titulo"
          >
            <header className="cobranca-modal__header">
              <div>
                <h3 id="cobranca-modal-titulo">{tituloModal()}</h3>
                {cobrancaModal.item?.valor != null && (
                  <p className="cobranca-modal__valor">
                    Valor: <strong>R$ {Number(cobrancaModal.item.valor).toFixed(2)}</strong>
                  </p>
                )}
              </div>
              <button type="button" className="cobranca-modal__fechar" onClick={fecharModal} aria-label="Fechar">
                ×
              </button>
            </header>

            <div className="cobranca-modal__body">
              {!cobranca && !cobrancaModal.forma && (
                <div className="cobranca-modal__opcoes">
                  <p className="field-hint">Escolha como deseja pagar esta mensalidade:</p>
                  <button type="button" className="cobranca-opcao cobranca-opcao--pix" onClick={() => setCobrancaModal((m) => ({ ...m, forma: 'PIX' }))}>
                    <span className="cobranca-opcao__titulo">PIX</span>
                    <span className="cobranca-opcao__desc">QR Code e código copia e cola</span>
                  </button>
                  <button type="button" className="cobranca-opcao cobranca-opcao--cartao" onClick={() => setCobrancaModal((m) => ({ ...m, forma: 'CREDIT_CARD' }))}>
                    <span className="cobranca-opcao__titulo">Cartão de crédito</span>
                    <span className="cobranca-opcao__desc">Pagamento online seguro</span>
                  </button>
                </div>
              )}

              {!cobranca && cobrancaModal.forma === 'PIX' && (
                <div className="cobranca-modal__confirmacao">
                  <p>Será gerada uma cobrança PIX para esta competência. Após o pagamento, o status será atualizado automaticamente.</p>
                </div>
              )}

              {!cobranca && cobrancaModal.forma === 'CREDIT_CARD' && (
                <div className="cobranca-modal__form">
                  <div className="form-grid">
                    <div className="cobranca-modal__field--full">
                      <label>Nome no cartão</label>
                      <input value={cartao.holderName} onChange={(e) => setCartao({ ...cartao, holderName: e.target.value })} autoComplete="cc-name" />
                    </div>
                    <div className="cobranca-modal__field--full">
                      <label>Número do cartão</label>
                      <input value={cartao.number} onChange={(e) => setCartao({ ...cartao, number: e.target.value })} inputMode="numeric" autoComplete="cc-number" />
                    </div>
                    <div>
                      <label>Validade (MM)</label>
                      <input value={cartao.expiryMonth} onChange={(e) => setCartao({ ...cartao, expiryMonth: e.target.value })} maxLength={2} autoComplete="cc-exp-month" />
                    </div>
                    <div>
                      <label>Validade (AAAA)</label>
                      <input value={cartao.expiryYear} onChange={(e) => setCartao({ ...cartao, expiryYear: e.target.value })} maxLength={4} autoComplete="cc-exp-year" />
                    </div>
                    <div>
                      <label>CVV</label>
                      <input value={cartao.ccv} onChange={(e) => setCartao({ ...cartao, ccv: e.target.value })} maxLength={4} autoComplete="cc-csc" />
                    </div>
                    <div>
                      <label>E-mail</label>
                      <input type="email" value={cartao.holderEmail} onChange={(e) => setCartao({ ...cartao, holderEmail: e.target.value })} autoComplete="email" />
                    </div>
                    <div>
                      <label>CPF do titular</label>
                      <input value={cartao.holderCpf} onChange={(e) => setCartao({ ...cartao, holderCpf: e.target.value })} />
                    </div>
                    <div>
                      <label>CEP</label>
                      <input value={cartao.holderPostalCode} onChange={(e) => setCartao({ ...cartao, holderPostalCode: e.target.value })} />
                    </div>
                    <div>
                      <label>Nº endereço</label>
                      <input value={cartao.holderAddressNumber} onChange={(e) => setCartao({ ...cartao, holderAddressNumber: e.target.value })} />
                    </div>
                    <div>
                      <label>Telefone</label>
                      <input value={cartao.holderPhone} onChange={(e) => setCartao({ ...cartao, holderPhone: e.target.value })} autoComplete="tel" />
                    </div>
                  </div>
                </div>
              )}

              {exibirPix && (
                <div className="cobranca-modal__pix">
                  {cobranca.pixQrCode && (
                    <div className="cobranca-modal__qr">
                      <img src={`data:image/png;base64,${cobranca.pixQrCode}`} alt="QR Code PIX" />
                    </div>
                  )}
                  {cobranca.pixCopiaCola && (
                    <div className="cobranca-modal__pix-copia">
                      <label>Código PIX copia e cola</label>
                      <textarea readOnly value={cobranca.pixCopiaCola} rows={3} />
                      <button type="button" className="btn-secondary btn-sm" onClick={copiarPix}>
                        {pixCopiado ? 'Copiado!' : 'Copiar código'}
                      </button>
                    </div>
                  )}
                  <p className="field-hint">Status: {cobranca.status}</p>
                </div>
              )}

              {exibirCartaoProcessado && (
                <div className="cobranca-modal__resultado">
                  <p>Pagamento com cartão enviado para processamento.</p>
                  <p className="field-hint">Status: {cobranca.status}</p>
                  {cobranca.urlPagamento && (
                    <a href={cobranca.urlPagamento} target="_blank" rel="noreferrer" className="btn-link">
                      Abrir link de pagamento
                    </a>
                  )}
                </div>
              )}
            </div>

            <footer className="cobranca-modal__footer">
              {!cobranca && cobrancaModal.forma && (
                <>
                  <button type="button" className="btn-primary" disabled={loading} onClick={gerarCobranca}>
                    {loading ? 'Processando...' : cobrancaModal.forma === 'CREDIT_CARD' ? 'Pagar' : 'Gerar cobrança PIX'}
                  </button>
                  <button type="button" className="btn-secondary" disabled={loading} onClick={() => setCobrancaModal((m) => ({ ...m, forma: null }))}>
                    Voltar
                  </button>
                </>
              )}

              {!cobranca && !cobrancaModal.forma && (
                <button type="button" className="btn-secondary" onClick={fecharModal}>
                  Cancelar
                </button>
              )}

              {cobranca && (
                <>
                  {modoLocal && cobranca.status === 'PENDENTE' && (
                    <button type="button" className="btn-primary" disabled={loading} onClick={simularPagamento}>
                      Simular pagamento (teste)
                    </button>
                  )}
                  {cobrancaModal.modo === 'visualizar' && cobranca.status === 'PENDENTE' && (
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => {
                        if (cobrancaModal.item) refazerCobranca(cobrancaModal.item);
                      }}
                    >
                      Refazer cobrança
                    </button>
                  )}
                  <button type="button" className="btn-secondary" onClick={fecharModal}>
                    Fechar
                  </button>
                </>
              )}
            </footer>
          </div>
        </div>
      )}

      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default PortalAlunoMensalidades;

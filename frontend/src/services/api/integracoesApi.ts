import { api } from './client';

export interface PagamentoInfo {
  message: string;
  modoLocal?: boolean;
  asaasAtivo?: boolean;
}

export interface CobrancaExternaResposta {
  id: number;
  tipo: string;
  status: string;
  valor?: number;
  urlPagamento?: string;
  billingType?: string;
  pixQrCode?: string;
  pixCopiaCola?: string;
  modoLocal?: boolean;
}

export const integracoesApi = {
  statusIntegracoes: () =>
    api.get<{ modoLocal: boolean; brevo: boolean; twilio: boolean; asaas: boolean }>('/integracoes/status'),

  portalPagamentoInfo: () =>
    api.get<PagamentoInfo>('/portal-aluno/pagamento-info'),

  portalHistoricoMensalidades: (ano: number) =>
    api.get<Array<{
      mes: number;
      ano: number;
      dataVencimento?: string;
      dataPagamento?: string;
      status: string;
      statusDescricao: string;
      valor?: number;
      cobrancaId?: number;
      podeGerarCobranca: boolean;
    }>>('/portal-aluno/mensalidades/historico', { params: { ano } }),

  consultarCobrancaMensalidade: (cobrancaId: number) =>
    api.get<CobrancaExternaResposta>(`/portal-aluno/cobranca/mensalidade/${cobrancaId}`),

  criarCobrancaMensalidade: (
    mes?: number,
    ano?: number,
    data?: { formaPagamento: string; cartao?: Record<string, string> },
  ) =>
    api.post<CobrancaExternaResposta>('/portal-aluno/cobranca/mensalidade', data ?? {}, {
      params: { mes, ano },
    }),

  simularPagamentoMensalidade: (cobrancaId: number) =>
    api.post<CobrancaExternaResposta>(`/portal-aluno/cobranca/mensalidade/${cobrancaId}/simular-pagamento`),

  criarCobrancaPlano: (instituicaoId: number, plano: string) =>
    api.post<CobrancaExternaResposta>(`/plano-instituicao/${instituicaoId}/cobranca`, { plano }),

  simularPagamentoPlano: (instituicaoId: number, cobrancaId: number) =>
    api.post<CobrancaExternaResposta>(
      `/plano-instituicao/${instituicaoId}/cobranca/${cobrancaId}/simular-pagamento`,
    ),
};

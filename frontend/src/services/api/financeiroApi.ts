import { api } from './client';

export const financeiroApi = {
  financeiroPlataformaResumo: () => api.get('/financeiro/plataforma/resumo'),

  financeiroDashboard: () => api.get('/financeiro/dashboard/resumo'),

  financeiroMensalidades: (mes: number, ano: number) =>
    api.get('/financeiro/mensalidades', { params: { mes, ano } }),

  financeiroInadimplentes: (mes: number, ano: number) =>
    api.get('/financeiro/inadimplentes', { params: { mes, ano } }),

  baixaMensalidade: (cpf: string) =>
    api.post(`/financeiro/mensalidades/${cpf.replace(/\D/g, '')}/baixa`),

  folhaPagamentoColaboradores: (mes: number, ano: number) =>
    api.get<Array<{
      cpf: string;
      nome: string;
      cargo: string;
      salarioBase: number;
      statusPagamento: string;
      reciboPublicado: boolean;
      diasTrabalhados?: number;
      minutosTrabalhados?: number;
      horasTrabalhadasFormatadas?: string;
      pontoMesConferidoRh?: boolean;
    }>>('/financeiro/folha-pagamento/colaboradores', { params: { mes, ano } }),

  folhaConfirmarPagamento: (data: {
    cpfColaborador: string;
    mesCompetencia: number;
    anoCompetencia: number;
    valorBruto?: number;
    valorLiquido?: number;
  }) => api.post<{ message: string; recibo: Record<string, unknown> }>(
    '/financeiro/folha-pagamento/confirmar-pagamento',
    data,
  ),

  financeiroPontoStatusIntegracao: (mes: number, ano: number) =>
    api.get<{
      pontoConferidoRh: boolean;
      integradoFinanceiro: boolean;
      colaboradoresComRegistro: number;
      totalMinutosInstituicao: number;
    }>('/financeiro/folha-pagamento/ponto/status-integracao', { params: { mes, ano } }),

  financeiroPontoIntegrar: (mes: number, ano: number) =>
    api.post<{ message: string }>('/financeiro/folha-pagamento/ponto/integrar', null, { params: { mes, ano } }),
};

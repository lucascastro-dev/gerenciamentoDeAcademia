import { api } from './client';

export const equipeApi = {
  meusDocumentosRemuneracao: (mes: number, ano: number) =>
    api.get<Array<{
      id: number;
      tipo: 'HOLERITE' | 'RECIBO' | 'INFORME';
      tipoDescricao: string;
      nomeColaborador: string;
      mesCompetencia: number;
      anoCompetencia: number;
      valorBruto?: number;
      valorLiquido?: number;
      conteudo?: string;
      publicadoEm?: string;
      possuiArquivoPdf?: boolean;
      nomeArquivo?: string;
    }>>('/colaborador/documentos-remuneracao', { params: { mes, ano } }),

  meuDocumentoRemuneracao: (id: number) =>
    api.get<{
      id: number;
      tipo: string;
      tipoDescricao: string;
      conteudo?: string;
      valorLiquido?: number;
      possuiArquivoPdf?: boolean;
      nomeArquivo?: string;
    }>(`/colaborador/documentos-remuneracao/${id}`),

  meuDocumentoRemuneracaoPdf: (id: number) =>
    api.get<Blob>(`/colaborador/documentos-remuneracao/${id}/pdf`, { responseType: 'blob' }),

  rhAnexarHoleritePdf: (formData: FormData) =>
    api.post('/rh/remuneracao/holerite/anexo', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),

  rhPublicarHolerite: (data: {
    cpfColaborador: string;
    mesCompetencia: number;
    anoCompetencia: number;
    valorBruto?: number;
    valorLiquido?: number;
    observacao?: string;
  }) => api.post('/rh/remuneracao/holerite/publicar', data),

  pontoStatusHoje: () =>
    api.get<{
      proximaAcao: 'ENTRADA' | 'SAIDA' | 'COMPLETO';
      horaEntrada?: string;
      horaSaida?: string;
      mensagem: string;
    }>('/colaborador/folha-ponto/status-hoje'),

  pontoMarcar: () =>
    api.post<{
      proximaAcao: 'ENTRADA' | 'SAIDA' | 'COMPLETO';
      horaEntrada?: string;
      horaSaida?: string;
      mensagem: string;
    }>('/colaborador/folha-ponto/marcar'),

  pontoMeuMes: (mes: number, ano: number) =>
    api.get<{
      mesCompetencia: number;
      anoCompetencia: number;
      registros: Array<{
        data: string;
        horaEntrada?: string;
        horaSaida?: string;
        minutosTrabalhados?: number;
        horasFormatadas: string;
        situacao: string;
      }>;
      totalMinutosTrabalhados: number;
      totalHorasFormatadas: string;
      diasComRegistroCompleto: number;
    }>('/colaborador/folha-ponto/meu-mes', { params: { mes, ano } }),

  pontoSolicitarAjuste: (data: {
    dataRegistro: string;
    horaEntradaProposta?: string;
    horaSaidaProposta?: string;
    justificativa: string;
  }) => api.post('/colaborador/folha-ponto/ajustes', data),

  pontoMeusAjustes: () =>
    api.get<Array<{
      id: number;
      dataRegistro: string;
      horaEntradaAtual?: string;
      horaSaidaAtual?: string;
      horaEntradaProposta?: string;
      horaSaidaProposta?: string;
      justificativa: string;
      status: string;
      observacaoGestor?: string;
    }>>('/colaborador/folha-ponto/ajustes'),

  rhPontoAjustesListar: (status?: string) =>
    api.get<Array<{
      id: number;
      nomeColaborador: string;
      dataRegistro: string;
      horaEntradaAtual?: string;
      horaSaidaAtual?: string;
      horaEntradaProposta?: string;
      horaSaidaProposta?: string;
      justificativa: string;
      status: string;
    }>>('/rh/folha-ponto/ajustes', { params: status ? { status } : undefined }),

  rhPontoAjusteDecidir: (id: number, data: { status: 'APROVADO' | 'REJEITADO'; observacaoGestor?: string }) =>
    api.post(`/rh/folha-ponto/ajustes/${id}/decidir`, data),

  rhFolhaPontoColaboradores: (mes: number, ano: number) =>
    api.get<Array<{
      cpf: string;
      nome: string;
      cargo: string;
      diasTrabalhados: number;
      minutosTrabalhados: number;
      horasFormatadas: string;
      possuiRegistroAberto: boolean;
    }>>('/rh/folha-ponto/colaboradores', { params: { mes, ano } }),

  rhFolhaPontoDetalhe: (cpf: string, mes: number, ano: number) =>
    api.get<{
      mesCompetencia: number;
      anoCompetencia: number;
      registros: Array<{
        data: string;
        horaEntrada?: string;
        horaSaida?: string;
        horasFormatadas: string;
        situacao: string;
      }>;
      totalHorasFormatadas: string;
      diasComRegistroCompleto: number;
    }>(`/rh/folha-ponto/colaboradores/${cpf}/detalhe`, { params: { mes, ano } }),

  rhFolhaPontoStatusIntegracao: (mes: number, ano: number) =>
    api.get<{
      pontoConferidoRh: boolean;
      integradoFinanceiro: boolean;
      colaboradoresComRegistro: number;
      totalMinutosInstituicao: number;
      conferidoEm?: string;
    }>('/rh/folha-ponto/status-integracao', { params: { mes, ano } }),

  rhFolhaPontoConferir: (mes: number, ano: number) =>
    api.post<{ message: string }>('/rh/folha-ponto/conferir', null, { params: { mes, ano } }),

  rhFolhaPontoReabrir: (mes: number, ano: number) =>
    api.post<{ message: string }>('/rh/folha-ponto/reabrir', null, { params: { mes, ano } }),

  feriasResumo: () =>
    api.get<{
      diasDisponiveisTotal: number;
      diasAprovadosTotal: number;
      diasPendentesTotal: number;
      periodos: Array<{
        inicio: string;
        fim: string;
        diasDireito: number;
        diasUtilizados: number;
        diasPendentes: number;
        diasDisponiveis: number;
        situacao: string;
      }>;
      solicitacoes: Array<{
        id: number;
        dataInicio: string;
        dataFim: string;
        diasSolicitados: number;
        status: string;
        statusDescricao: string;
        observacaoRh?: string;
      }>;
    }>('/colaborador/ferias/resumo'),

  feriasSolicitar: (data: { dataInicio: string; dataFim: string }) =>
    api.post('/colaborador/ferias/solicitar', data),

  feriasCancelar: (id: number) =>
    api.post(`/colaborador/ferias/${id}/cancelar`),

  rhFeriasListar: (status?: string) =>
    api.get<Array<{
      id: number;
      cpfColaborador: string;
      nomeColaborador: string;
      dataInicio: string;
      dataFim: string;
      diasSolicitados: number;
      status: string;
      statusDescricao: string;
      observacaoRh?: string;
    }>>('/rh/ferias', { params: status ? { status } : {} }),

  rhFeriasDecidir: (id: number, data: { status: 'APROVADO' | 'REJEITADO'; observacaoRh?: string }) =>
    api.post(`/rh/ferias/${id}/decidir`, data),
};

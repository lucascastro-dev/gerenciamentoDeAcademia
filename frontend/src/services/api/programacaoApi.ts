import { api } from './client';

export const programacaoApi = {
  programacaoTipos: (instituicaoId: string | number) =>
    api.get<Array<{ codigo: string; descricao: string }>>(`/instituicao/${instituicaoId}/programacao/tipos`),

  programacaoListarItens: (instituicaoId: string | number) =>
    api.get(`/instituicao/${instituicaoId}/programacao/itens`),

  programacaoCriarItem: (instituicaoId: string | number, data: Record<string, unknown>) =>
    api.post(`/instituicao/${instituicaoId}/programacao/itens`, data),

  programacaoAtualizarItem: (instituicaoId: string | number, id: number, data: Record<string, unknown>) =>
    api.put(`/instituicao/${instituicaoId}/programacao/itens/${id}`, data),

  programacaoExcluirItem: (instituicaoId: string | number, id: number) =>
    api.delete(`/instituicao/${instituicaoId}/programacao/itens/${id}`),

  programacaoValidarConflito: (
    instituicaoId: string | number,
    data: Record<string, unknown>,
    ignorarId?: number,
  ) =>
    api.post(`/instituicao/${instituicaoId}/programacao/itens/validar-conflito`, data, {
      params: ignorarId ? { ignorarId } : undefined,
    }),

  programacaoGrade: (instituicaoId: string | number, semana?: string) =>
    api.get(`/instituicao/${instituicaoId}/programacao/grade`, { params: semana ? { semana } : undefined }),

  programacaoListarSalas: (instituicaoId: string | number) =>
    api.get(`/instituicao/${instituicaoId}/programacao/salas`),

  programacaoCriarSala: (instituicaoId: string | number, data: Record<string, unknown>) =>
    api.post(`/instituicao/${instituicaoId}/programacao/salas`, data),

  programacaoExcluirSala: (instituicaoId: string | number, salaId: number) =>
    api.delete(`/instituicao/${instituicaoId}/programacao/salas/${salaId}`),
};

export const portalAlunoApi = {
  portalAlunoDados: () => api.get('/portal-aluno/meus-dados'),

  portalAlunoTurmas: () => api.get('/portal-aluno/minhas-turmas'),

  portalAlunoMensalidade: () => api.get('/portal-aluno/mensalidade'),

  portalAlunoPagamentoInfo: () => api.get<{ message: string }>('/portal-aluno/pagamento-info'),

  portalAlunoAlterarSenha: (data: { senhaAtual: string; senhaNova: string }) =>
    api.put('/portal-aluno/alterar-senha', data),

  portalAlunoProgramacao: () =>
    api.get<Array<{
      id: number;
      tipo: string;
      tipoDescricao: string;
      titulo: string;
      descricao?: string;
      dataPrevista?: string;
      horario?: string;
      sala?: string;
    }>>('/portal-aluno/minha-programacao'),
};

export const plataformaApi = {
  dashboardPlataformaResumo: () => api.get('/dashboard/plataforma/resumo'),

  dashboardResumo: () => api.get('/dashboard/resumo'),

  gerarCertificados: (data: Record<string, unknown>) =>
    api.post<{
      mensagem: string;
      nomeArquivoResumo: string;
      conteudoResumo: string;
    }>('/certificado/gerarCertificadoJudo', data),

  listarAuditoria: () =>
    api.get<Array<{
      id?: number;
      ajuste: string;
      dataHora: string;
      usuarioLogin: string;
      entidade: string;
      referencia?: string;
      motivo?: string;
    }>>('/auditoria/lista'),
};

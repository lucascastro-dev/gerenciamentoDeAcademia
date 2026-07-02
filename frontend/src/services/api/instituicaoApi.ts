import { api, limparCnpj } from './client';

export const instituicaoApi = {
  listarTodasInstituicoes: () => api.get<Array<{ id: number; razaoSocial: string; cadastroAtivo?: boolean }>>(
    '/instituicao/consultarTodasAcademias',
  ),

  cadastrarInstituicao: (data: Record<string, unknown>) =>
    api.post('/instituicao/registrarAcademia', data),

  cadastrarEmpresa: (data: Record<string, unknown>) =>
    api.post('/instituicao/registrarAcademia', data),

  solicitarPrimeiroAcesso: (cpf: string) =>
    api.put(`/instituicao/solicitarPrimeiroAcesso/${cpf.replace(/\D/g, '')}`),

  ativarFuncionarioInstituicao: (
    instituicaoId: string | number,
    cpf: string,
    data: Record<string, unknown>,
  ) => api.post(`/instituicao/instituicao/${instituicaoId}/ativarFuncionario/${cpf.replace(/\D/g, '')}`, data),

  inativarFuncionarioInstituicao: (instituicaoId: string | number, cpf: string) =>
    api.post(`/instituicao/instituicao/${instituicaoId}/inativarFuncionario/${cpf.replace(/\D/g, '')}`),

  desativarInstituicao: (cnpj: string) =>
    api.delete(`/instituicao/desativarAcademia/${limparCnpj(cnpj)}`),

  ativarCadastroInstituicao: (cnpj: string, plano: string) =>
    api.post(`/instituicao/ativarCadastro/${limparCnpj(cnpj)}`, { plano }),

  trocarAdministradorInstituicao: (data: { cnpj: string; cpfAdministrador: string }) =>
    api.put('/instituicao/administrador', {
      cnpj: limparCnpj(data.cnpj),
      cpfAdministrador: data.cpfAdministrador.replace(/\D/g, ''),
    }),

  listarInstituicoesResumo: () =>
    api.get<Array<{
      id: number;
      razaoSocial: string;
      cnpj?: string;
      cnpjExibicao?: string;
      plano?: string;
      planoExibicao?: string;
      statusFinanceiro?: string;
      statusFinanceiroExibicao?: string;
      cadastroAtivo?: boolean;
      statusCadastroExibicao?: string;
    }>>('/instituicao/lista'),

  consultarInstituicaoDetalheCnpj: (cnpj: string) =>
    api.get(`/instituicao/detalheCnpj/${limparCnpj(cnpj)}`),

  ativarInstituicao: (data: { cnpj: string; cpfAdministrador: string; plano: string }) =>
    api.post('/instituicao/ativarUnidade', data),

  atualizarStatusFinanceiro: (data: { cnpj: string; statusFinanceiro: string }) =>
    api.put('/instituicao/statusFinanceiro', data),

  atualizarPlanoInstituicao: (data: { cnpj: string; plano: string }) =>
    api.put('/instituicao/plano', {
      cnpj: limparCnpj(data.cnpj),
      plano: data.plano,
    }),

  desativarAcademia: (cnpj: string) =>
    api.delete(`/instituicao/desativarAcademia/${cnpj}`),

  consultarInstituicao: (instituicaoId: string | number) =>
    api.get(`/instituicao/consultarAcademiaId/${instituicaoId}`),

  consultarAcademia: (instituicaoId: string | number) =>
    api.get(`/instituicao/consultarAcademiaId/${instituicaoId}`),

  consultarInstituicaoPorCnpj: (cnpj: string) =>
    api.get(`/instituicao/consultarAcademiaCnpj/${cnpj}`),

  consultarAcademiaPorCnpj: (cnpj: string) =>
    api.get(`/instituicao/consultarAcademiaCnpj/${cnpj}`),

  editarInstituicao: (data: Record<string, unknown>) =>
    api.put('/instituicao/atualizarDadosAcademia', data),

  editarAcademia: (data: Record<string, unknown>) =>
    api.put('/instituicao/atualizarDadosAcademia', data),

  planoInstituicao: (instituicaoId: string | number) =>
    api.get(`/plano-instituicao/${instituicaoId}`),

  tiposPlanoInstituicao: () => api.get<Array<{ codigo: string; descricao: string; dias: string }>>('/plano-instituicao/tipos'),

  ativarPlanoInstituicao: (instituicaoId: string | number, plano: string) =>
    api.put(`/plano-instituicao/${instituicaoId}/ativar`, { plano }),
};

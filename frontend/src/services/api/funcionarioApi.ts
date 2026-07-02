import { api } from './client';

export const funcionarioApi = {
  cadastrarPessoa: (data: Record<string, unknown>) =>
    api.post('/funcionario/cadastrarFuncionario', data),

  preCadastroColaborador: (data: Record<string, unknown>) =>
    api.post('/funcionario/preCadastroColaborador', data),

  definirSubMaster: (cpf: string, habilitar: boolean) =>
    api.put(`/funcionario/${cpf.replace(/\D/g, '')}/sub-master`, { habilitar }),

  listarTiposFuncionario: () => api.get('/funcionario/tipos'),

  listarFuncionariosResumo: () =>
    api.get<Array<{
      id: number;
      vinculoId?: number;
      nome: string;
      cpf?: string;
      cpfExibicao?: string;
      dataDeNascimento?: string;
      cargo?: string;
      instituicaoId?: number;
      instituicaoNome?: string;
    }>>('/funcionario/lista'),

  consultarFuncionarioPorCpf: (cpf: string) =>
    api.get<{
      nome: string;
      rg: string;
      cpf: string;
      dataDeNascimento: string;
      endereco: string;
      telefone: string;
      email?: string;
      cadastroAtivo?: boolean;
      permitirGerenciarFuncoes?: boolean;
      vinculos?: Array<{
        vinculoId?: number;
        instituicaoId: number;
        razaoSocial: string;
        tipoFuncionario: string;
        areaTerceirizado?: string;
        especializacao?: string;
        cargo?: string;
      }>;
    }>(`/funcionario/consultarPorCpf/${cpf}`),

  editarPessoa: (data: Record<string, unknown>) =>
    api.put('/funcionario/editarFuncionario', data),

  atualizarVinculoFuncionario: (data: Record<string, unknown>) =>
    api.put('/funcionario/vinculo', data),

  meuPerfil: () => api.get('/funcionario/meuPerfil'),

  atualizarMeuPerfil: (data: Record<string, unknown>) =>
    api.put('/funcionario/meuPerfil', data),

  alterarSenha: (data: { senhaAtual: string; senhaNova: string }) =>
    api.put('/funcionario/alterarSenha', data),

  listarFuncionarios: () => api.get('/funcionario/consultarFuncionario'),

  auditoriaFuncionario: (id: number) => api.get(`/funcionario/revision/${id}`),
};

import axios from 'axios';
import { carregarSessao } from '../auth/permissoes';

/** Docker: URL relativa (nginx faz proxy). Dev local: VITE_API_URL=http://localhost:8000/srv-gerenciaracademia */
const BASE_URL =
  import.meta.env.VITE_API_URL || '/srv-gerenciaracademia';

const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
  const sessao = carregarSessao();
  const token = sessao?.token || localStorage.getItem('@App:token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error?.response?.data;
    const code = typeof data === 'object' && data !== null ? (data as { code?: string }).code : undefined;
    const path = window.location.pathname;
    const emAreaPublica = path.startsWith('/areapublica');
    const emAreaLogada = path.startsWith('/arealogada');
    const temSessao = !!localStorage.getItem('@App:token');

    // Bloqueio no login: mensagem fica na tela de login (não redirecionar).
    if (code === 'COBRANCA_BLOQUEADA') {
      return Promise.reject(error);
    }

    if (
      (code === 'PLANO_INSTITUICAO_INATIVO' || code === 'PAGAMENTO_PENDENTE')
      && emAreaLogada
      && temSessao
      && !emAreaPublica
      && !path.includes('plano-instituicao')
      && !path.includes('meu-perfil')
    ) {
      window.location.href = '/arealogada/plano-instituicao';
    }
    return Promise.reject(error);
  },
);

export interface LoginResponse {
  token: string;
  nome: string;
  tipoFuncionario: string | null;
  usuarioMaster: boolean;
  perfilExibicao?: string | null;
  masterRaiz?: boolean;
  acessoFinanceiroCompleto?: boolean;
  permissoes: string[];
  tipoAcesso?: 'COLABORADOR' | 'ALUNO';
  planoInstituicaoAtivo?: boolean;
  situacaoCobranca?: 'ATIVO' | 'EM_TOLERANCIA' | 'BLOQUEADO';
  alertaCobranca?: boolean;
  mensagemAlertaCobranca?: string | null;
}

const HttpService = {
  login: (login: string, password: string, vinculo: string) =>
    api.post<LoginResponse>('/login', { login, password, vinculo }),

  listarVinculos: (cpf: string) =>
    api.get<Array<{ id: number; razaoSocial: string; cadastroAtivo?: boolean; selecionavel?: boolean }>>(
      `/login/vinculos/${cpf}`,
    ),

  listarTodasInstituicoes: () => api.get<Array<{ id: number; razaoSocial: string; cadastroAtivo?: boolean }>>(
    '/instituicao/consultarTodasAcademias',
  ),

  dashboardPlataformaResumo: () => api.get('/dashboard/plataforma/resumo'),

  financeiroPlataformaResumo: () => api.get('/financeiro/plataforma/resumo'),

  solicitarRecuperacaoSenha: (cpf: string) =>
    api.post<{ message: string }>('/login/solicitarRecuperacaoSenha', { cpf }),

  cadastrarPessoa: (data: Record<string, unknown>) =>
    api.post('/funcionario/cadastrarFuncionario', data),

  cadastrarInstituicao: (data: Record<string, unknown>) =>
    api.post('/instituicao/registrarAcademia', data),

  /** @deprecated use cadastrarInstituicao */
  cadastrarEmpresa: (data: Record<string, unknown>) =>
    api.post('/instituicao/registrarAcademia', data),

  preCadastroColaborador: (data: Record<string, unknown>) =>
    api.post('/funcionario/preCadastroColaborador', data),

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
    api.delete(`/instituicao/desativarAcademia/${cnpj.replace(/\D/g, '')}`),

  ativarCadastroInstituicao: (cnpj: string, plano: string) =>
    api.post(`/instituicao/ativarCadastro/${cnpj.replace(/\D/g, '')}`, { plano }),

  trocarAdministradorInstituicao: (data: { cnpj: string; cpfAdministrador: string }) =>
    api.put('/instituicao/administrador', {
      cnpj: data.cnpj.replace(/\D/g, ''),
      cpfAdministrador: data.cpfAdministrador.replace(/\D/g, ''),
    }),

  consultarInstituicaoDetalheCnpj: (cnpj: string) =>
    api.get(`/instituicao/detalheCnpj/${cnpj.replace(/\D/g, '')}`),

  ativarInstituicao: (data: { cnpj: string; cpfAdministrador: string; plano: string }) =>
    api.post('/instituicao/ativarUnidade', data),

  atualizarStatusFinanceiro: (data: { cnpj: string; statusFinanceiro: string }) =>
    api.put('/instituicao/statusFinanceiro', data),

  atualizarPlanoInstituicao: (data: { cnpj: string; plano: string }) =>
    api.put('/instituicao/plano', {
      cnpj: data.cnpj.replace(/\D/g, ''),
      plano: data.plano,
    }),

  definirSubMaster: (cpf: string, habilitar: boolean) =>
    api.put(`/funcionario/${cpf.replace(/\D/g, '')}/sub-master`, { habilitar }),

  /** @deprecated use desativarInstituicao */
  desativarAcademia: (cnpj: string) =>
    api.delete(`/instituicao/desativarAcademia/${cnpj}`),

  listarTiposFuncionario: () => api.get('/funcionario/tipos'),

  consultarInstituicao: (instituicaoId: string | number) =>
    api.get(`/instituicao/consultarAcademiaId/${instituicaoId}`),

  /** @deprecated use consultarInstituicao */
  consultarAcademia: (instituicaoId: string | number) =>
    api.get(`/instituicao/consultarAcademiaId/${instituicaoId}`),

  consultarFuncionarioPorCpf: (cpf: string) =>
    api.get(`/funcionario/consultarPorCpf/${cpf}`),

  editarPessoa: (data: Record<string, unknown>) =>
    api.put('/funcionario/editarFuncionario', data),

  consultarInstituicaoPorCnpj: (cnpj: string) =>
    api.get(`/instituicao/consultarAcademiaCnpj/${cnpj}`),

  /** @deprecated use consultarInstituicaoPorCnpj */
  consultarAcademiaPorCnpj: (cnpj: string) =>
    api.get(`/instituicao/consultarAcademiaCnpj/${cnpj}`),

  editarInstituicao: (data: Record<string, unknown>) =>
    api.put('/instituicao/atualizarDadosAcademia', data),

  /** @deprecated use editarInstituicao */
  editarAcademia: (data: Record<string, unknown>) =>
    api.put('/instituicao/atualizarDadosAcademia', data),

  listarAlunos: (instituicaoId: string | number) =>
    api.get('/aluno/consultarAluno', { params: { instituicaoId } }),

  consultarAluno: (cpf: string, instituicaoId: string | number) =>
    api.get(`/aluno/consultarAluno/${cpf.replace(/\D/g, '')}`, { params: { instituicaoId } }),

  consultarAlunoPorCpf: (cpf: string) =>
    api.get<{
      nome: string;
      cpf: string;
      rg: string;
      dataDeNascimento: string;
      endereco: string;
      telefone: string;
      email?: string;
      valorMensalidade?: number;
      diaVencimentoMensalidade?: number;
      nomeResponsavel?: string;
      telefoneResponsavel?: string;
      matriculas: Array<{
        instituicaoId: number;
        razaoSocial: string;
        turmas: Array<{ id: number; modalidade: string; horario: string; sala?: string }>;
      }>;
    }>(`/aluno/consultarPorCpf/${cpf.replace(/\D/g, '')}`),

  matricularAluno: (data: Record<string, unknown>) =>
    api.post('/aluno/matricularAluno', data),

  alterarAluno: (data: Record<string, unknown>) =>
    api.put('/aluno/alterarAluno', data),

  desmatricularAluno: (cpf: string) =>
    api.delete(`/aluno/desmatricularAluno/${cpf}`),

  listarTurmas: (params?: { instituicaoId?: number; professorCpf?: string; dias?: string[] }) =>
    api.get('/turma/listarTurmas', { params }),

  montarTurma: (data: Record<string, unknown>) =>
    api.post('/turma/montarTurma', data),

  professoresInstituicao: (instituicaoId: string | number) =>
    api.get<Array<{ cpf: string; nome: string }>>('/turma/professores', { params: { instituicaoId } }),

  vincularProfessorTurma: (turmaId: number, cpfProfessor: string) =>
    api.put(`/turma/${turmaId}/professor`, { cpfProfessor: cpfProfessor || null }),

  alterarTurma: (data: Record<string, unknown>) =>
    api.put('/turma/Alterar', data),

  baixaMensalidade: (cpf: string) =>
    api.post(`/financeiro/mensalidades/${cpf.replace(/\D/g, '')}/baixa`),

  excluirTurma: (id: number) => api.delete(`/turma/excluirTurma/${id}`),

  minhasTurmasProfessor: () => api.get('/turma/professor/minhas'),

  alunosDaTurma: (id: string | number) => api.get(`/turma/${id}/alunos`),

  dashboardResumo: () => api.get('/dashboard/resumo'),

  financeiroDashboard: () => api.get('/financeiro/dashboard/resumo'),

  financeiroMensalidades: () => api.get('/financeiro/mensalidades'),

  financeiroInadimplentes: () => api.get('/financeiro/inadimplentes'),

  gerarCertificados: (data: Record<string, unknown>) =>
    api.post('/certificado/gerarCertificadoJudo', data),

  meuPerfil: () => api.get('/funcionario/meuPerfil'),

  atualizarMeuPerfil: (data: Record<string, unknown>) =>
    api.put('/funcionario/meuPerfil', data),

  alterarSenha: (data: { senhaAtual: string; senhaNova: string }) =>
    api.put('/funcionario/alterarSenha', data),

  listarFuncionarios: () => api.get('/funcionario/consultarFuncionario'),

  auditoriaFuncionario: (id: number) => api.get(`/funcionario/revision/${id}`),

  planoInstituicao: (instituicaoId: string | number) =>
    api.get(`/plano-instituicao/${instituicaoId}`),

  tiposPlanoInstituicao: () => api.get<Array<{ codigo: string; descricao: string; dias: string }>>('/plano-instituicao/tipos'),

  ativarPlanoInstituicao: (instituicaoId: string | number, plano: string) =>
    api.put(`/plano-instituicao/${instituicaoId}/ativar`, { plano }),

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

export default HttpService;

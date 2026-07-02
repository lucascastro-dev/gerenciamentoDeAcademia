import { api } from './client';

export const alunoApi = {
  listarAlunos: (instituicaoId: string | number) =>
    api.get('/aluno/consultarAluno', { params: { instituicaoId } }),

  listarAlunosResumo: () =>
    api.get<Array<{ id: number; nome: string; cpf?: string; cpfExibicao?: string; dataDeNascimento?: string }>>('/aluno/lista'),

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

  consultarAlunoProfessor: (cpf: string) =>
    api.get<{
      nome: string;
      cpfMascarado: string;
      rgMascarado: string;
      dataDeNascimento: string;
      enderecoResumido: string;
      telefoneMascarado: string;
      emailMascarado: string;
      nomeResponsavel?: string;
      telefoneResponsavelMascarado?: string;
      turmasInstituicao: Array<{ id: number; modalidade: string; horario: string; sala?: string }>;
    }>(`/aluno/professor/consultar/${cpf.replace(/\D/g, '')}`),

  consultarAlunoProfessorPorId: (alunoId: number) =>
    api.get<{
      nome: string;
      cpfMascarado: string;
      rgMascarado: string;
      dataDeNascimento: string;
      enderecoResumido: string;
      telefoneMascarado: string;
      emailMascarado: string;
      nomeResponsavel?: string;
      telefoneResponsavelMascarado?: string;
      turmasInstituicao: Array<{ id: number; modalidade: string; horario: string; sala?: string }>;
    }>(`/aluno/professor/consultar/id/${alunoId}`),
};

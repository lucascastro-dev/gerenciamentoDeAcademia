import { api } from './client';

export const turmaApi = {
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

  excluirTurma: (id: number) => api.delete(`/turma/excluirTurma/${id}`),

  minhasTurmasProfessor: () => api.get('/turma/professor/minhas'),

  alunosDaTurma: (id: string | number) => api.get(`/turma/${id}/alunos`),

  adicionarAlunoTurmaProfessor: (turmaId: string | number, cpf: string) =>
    api.post(`/turma/professor/${turmaId}/alunos`, { cpf: cpf.replace(/\D/g, '') }),

  removerAlunoTurmaProfessor: (turmaId: string | number, cpf: string) =>
    api.delete(`/turma/professor/${turmaId}/alunos/${cpf.replace(/\D/g, '')}`),

  presencaConsultar: (turmaId: string | number, ano: number, mes: number) =>
    api.get(`/turma/professor/${turmaId}/presenca`, { params: { ano, mes } }),

  presencaSalvar: (turmaId: string | number, data: Record<string, unknown>) =>
    api.put(`/turma/professor/${turmaId}/presenca`, data),

  presencaPdf: (turmaId: string | number, ano: number, mes: number) =>
    api.get(`/turma/professor/${turmaId}/presenca/pdf`, {
      params: { ano, mes },
      responseType: 'blob',
    }),
};

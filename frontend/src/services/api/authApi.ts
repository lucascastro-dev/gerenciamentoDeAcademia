import { api, type LoginResponse } from './client';

export const authApi = {
  login: (login: string, password: string, vinculo: string) =>
    api.post<LoginResponse>('/login', { login, password, vinculo }),

  listarVinculos: (cpf: string) =>
    api.get<Array<{ id: number; razaoSocial: string; cadastroAtivo?: boolean; selecionavel?: boolean }>>(
      `/login/vinculos/${cpf}`,
    ),

  solicitarRecuperacaoSenha: (cpf: string) =>
    api.post<{ message: string }>('/login/solicitarRecuperacaoSenha', { cpf }),
};

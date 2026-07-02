import { api } from './client';

export interface ContatoPublicoPayload {
  nome: string;
  email: string;
  telefone?: string;
  instituicao?: string;
  mensagem: string;
}

export function enviarContatoPublico(payload: ContatoPublicoPayload) {
  return api.post<{ message: string }>('/publico/contato', payload);
}

import { AxiosError } from 'axios';

/** Extrai mensagem tratada pelo backend (corpo string ou JSON). */
export function extractApiMessage(error: unknown, fallback = 'Erro na operação.'): string {
  const axiosError = error as AxiosError<unknown>;
  const data = axiosError.response?.data;

  if (typeof data === 'string' && data.trim()) {
    return data.trim();
  }

  if (data && typeof data === 'object') {
    const obj = data as Record<string, unknown>;
    const msg = obj.message ?? obj.error ?? obj.msg;
    if (typeof msg === 'string' && msg.trim()) {
      return msg.trim();
    }
  }

  const status = axiosError.response?.status;
  if (status === 401) {
    return 'Usuário ou senha inválidos. Verifique o CPF, a senha e a instituição selecionada.';
  }
  if (status === 403) {
    return 'Você não tem permissão para esta ação.';
  }

  if (axiosError.message && !axiosError.message.startsWith('Request failed')) {
    return axiosError.message;
  }

  return fallback;
}

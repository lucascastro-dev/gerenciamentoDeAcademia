import axios from 'axios';
import { carregarSessao } from '../../auth/permissoes';

/** Docker: URL relativa (nginx faz proxy). Dev local: VITE_API_URL=http://localhost:8000/srv-gerenciaracademia */
export const BASE_URL =
  import.meta.env.VITE_API_URL || '/srv-gerenciaracademia';

export const api = axios.create({ baseURL: BASE_URL });

export const limparCnpj = (cnpj: string) => cnpj.replace(/[^A-Za-z0-9]/g, '').toUpperCase();

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
    const emAreaPublica = path.startsWith('/areapublica') || path === '/entrar';
    const emAreaLogada = path.startsWith('/arealogada');
    const temSessao = !!localStorage.getItem('@App:token');

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

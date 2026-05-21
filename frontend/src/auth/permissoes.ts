export type TipoFuncionario =
  | 'DIRETOR'
  | 'FINANCEIRO'
  | 'RH'
  | 'TI'
  | 'ADMINISTRADOR'
  | 'RECEPCIONISTA'
  | 'SERVICOS_GERAIS'
  | 'PROFESSOR'
  | 'ESTAGIARIO'
  | 'TERCEIRIZADO';

export type TipoAcesso = 'COLABORADOR' | 'ALUNO';

export interface SessaoUsuario {
  token: string;
  cpf: string;
  vinculo: string;
  nome: string;
  tipoFuncionario: TipoFuncionario | null;
  usuarioMaster: boolean;
  permissoes: string[];
  tipoAcesso?: TipoAcesso;
  planoInstituicaoAtivo?: boolean;
}

export function isPortalAluno(sessao: SessaoUsuario | null): boolean {
  return sessao?.tipoAcesso === 'ALUNO';
}

const STORAGE_KEY = '@App:sessao';

export function salvarSessao(sessao: SessaoUsuario) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(sessao));
  localStorage.setItem('@App:token', sessao.token);
  localStorage.setItem('@App:cpf', sessao.cpf);
  localStorage.setItem('@App:vinculo', sessao.vinculo);
}

export function carregarSessao(): SessaoUsuario | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as SessaoUsuario;
  } catch {
    return null;
  }
}

export function limparSessao() {
  localStorage.clear();
}

export function possuiPermissao(sessao: SessaoUsuario | null, codigo: string): boolean {
  if (!sessao) return false;
  if (sessao.usuarioMaster) return true;
  return sessao.permissoes.includes(codigo);
}

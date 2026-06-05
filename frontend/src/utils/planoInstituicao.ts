export interface TipoPlanoOpcao {
  codigo: string;
  descricao: string;
}

/** Planos exibidos no cadastro (sem trimestral, conforme regra de negócio da tela). */
const CODIGOS_NOVA_INSTITUICAO = new Set([
  'TRIAL_7_DIAS',
  'MENSAL',
  'SEMESTRAL',
  'ANUAL',
]);

export function filtrarTiposPlano(
  tipos: TipoPlanoOpcao[],
  trialUtilizado?: boolean,
  somenteNovaInstituicao = false,
): TipoPlanoOpcao[] {
  return (tipos || []).filter((t) => {
    if (trialUtilizado && t.codigo === 'TRIAL_7_DIAS') {
      return false;
    }
    if (somenteNovaInstituicao && !CODIGOS_NOVA_INSTITUICAO.has(t.codigo)) {
      return false;
    }
    return true;
  });
}

export function descricaoPlano(codigo: string, tipos: TipoPlanoOpcao[]): string {
  return tipos.find((t) => t.codigo === codigo)?.descricao || codigo.replace(/_/g, ' ');
}

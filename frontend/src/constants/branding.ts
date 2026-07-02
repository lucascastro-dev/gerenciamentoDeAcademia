/** Nome comercial do produto (SaaS de gestão educacional) */
export const APP_NAME = 'Turma360';

export const APP_TAGLINE = 'Gestão completa da sua instituição em um só lugar';

/** Paleta de marca (CSS: --brand-*) */
export const BRAND_COLORS = {
  primary: '#2563EB',
  accent: '#06B6D4',
  energy: '#F59E0B',
} as const;

/** Termo de negócio no lugar de "academia" na interface */
export const INSTITUICAO = {
  singular: 'instituição',
  plural: 'instituições',
  capitalized: 'Instituição',
  pluralCapitalized: 'Instituições',
  vinculoLabel: 'Código da instituição',
  masterOnlyHint: 'Cadastro de novas instituições no sistema é exclusivo do administrador da plataforma.',
};

/** Rótulo do módulo de colaboradores (ex-RH) */
export const EQUIPE = {
  menuTitulo: 'Gestão de equipe',
  holeriteAnexoTitulo: 'Anexar holerite / recibo (PDF)',
  holeriteAnexoHint:
    'Envie o holerite ou recibo em PDF gerado pelo sistema de folha da sua instituição. O colaborador consulta em Meu holerite.',
};

/** Planos comerciais exibidos no site (valores de referência para contato comercial) */

export interface PlanoComercial {
  codigo: string;
  nome: string;
  preco: string;
  periodo: string;
  destaque?: boolean;
  badge?: string;
  recursos: string[];
}

export const PLANOS_COMERCIAIS: PlanoComercial[] = [
  {
    codigo: 'TRIAL_7_DIAS',
    nome: 'Teste grátis',
    preco: 'R$ 0',
    periodo: '7 dias',
    badge: 'Comece aqui',
    recursos: [
      'Acesso completo à plataforma',
      'Até 50 alunos no período de teste',
      'Suporte por e-mail',
      'Sem cartão de crédito',
    ],
  },
  {
    codigo: 'MENSAL',
    nome: 'Mensal',
    preco: 'R$ 199',
    periodo: '/mês',
    recursos: [
      'Alunos e turmas ilimitados',
      'Portal do aluno',
      'Cobrança escolar',
      'Gestão de equipe',
      'Suporte em horário comercial',
    ],
  },
  {
    codigo: 'TRIMESTRAL',
    nome: 'Trimestral',
    preco: 'R$ 537',
    periodo: '/trimestre',
    destaque: true,
    badge: 'Mais escolhido',
    recursos: [
      'Tudo do plano Mensal',
      '10% de economia',
      'Prioridade no suporte',
      'Treinamento de implantação',
    ],
  },
  {
    codigo: 'SEMESTRAL',
    nome: 'Semestral',
    preco: 'R$ 999',
    periodo: '/semestre',
    recursos: [
      'Tudo do plano Trimestral',
      '16% de economia',
      'Acompanhamento de onboarding',
    ],
  },
  {
    codigo: 'ANUAL',
    nome: 'Anual',
    preco: 'R$ 1.899',
    periodo: '/ano',
    recursos: [
      'Tudo do plano Semestral',
      '20% de economia',
      'Consultoria de configuração inicial',
    ],
  },
];

export const RECURSOS_TODOS_PLANOS = [
  'Turmas, matrículas e programação',
  'Portal do aluno responsivo',
  'Permissões por perfil',
  'Auditoria de alterações',
  'Atualizações incluídas',
];

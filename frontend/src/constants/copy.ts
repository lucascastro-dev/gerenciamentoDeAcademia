/** Textos de produto — tom profissional, claro e acolhedor */

import { APP_NAME, APP_TAGLINE } from './branding';



/** Dados de contato comercial — configure via variáveis de ambiente em produção */

export const CONTATO = {

  email: import.meta.env.VITE_CONTATO_EMAIL ?? 'contato@turma360.com.br',

  whatsapp: import.meta.env.VITE_CONTATO_WHATSAPP ?? '5500000000000',

  whatsappLabel: import.meta.env.VITE_CONTATO_WHATSAPP_LABEL ?? '(00) 00000-0000',

  horario: 'Segunda a sexta, das 9h às 18h',

} as const;

/** Placeholders genéricos para formulários */

export const PLACEHOLDERS = {

  cpf: '000.000.000-00',

  cnpj: '00.000.000/0000-00',

  cep: '00000-000',

  telefone: '(00) 00000-0000',

  telefoneInternacional: '00 00000 0000',

  email: 'seuemail@exemplo.com',

  emailInstituicao: 'contato@instituicao.edu.br',

  senha: 'Digite sua senha',

  senhaNova: 'Crie uma senha forte',

  senhaConfirmacao: 'Repita a senha',

  nomeCompleto: 'Nome completo conforme documento',

  nomeAluno: 'Nome do aluno',

  rg: 'Número do documento',

  instituicao: 'Nome da escola ou academia',

  filtro: 'Digite para filtrar…',

  modalidade: 'Ex.: Judô adulto',

  sala: 'Sala ou dojo',

  mensagemContato:

    'Descreva sua instituição, quantidade de alunos e o que você gostaria de saber sobre os planos.',

} as const;



export function whatsappConfigurado(): boolean {

  const num = CONTATO.whatsapp.replace(/\D/g, '');

  return num.length >= 10 && !/^0+$/.test(num);

}



export function linkWhatsApp(mensagem?: string): string {

  if (!whatsappConfigurado()) {

    return '/contato';

  }

  const texto = encodeURIComponent(

    mensagem ?? `Olá! Gostaria de saber mais sobre o ${APP_NAME} para minha instituição.`,

  );

  return `https://wa.me/${CONTATO.whatsapp.replace(/\D/g, '')}?text=${texto}`;

}



export const COPY = {

  tagline: APP_TAGLINE,

  heroEyebrow: 'Gestão educacional integrada',

  heroLead:

    'Centralize turmas, matrículas, cobrança escolar e portal do aluno em uma plataforma feita para escolas, cursos livres, esportes e idiomas.',

  microcopyLogin: [

    'Um único portal de acesso',

    'Selecione a instituição após o CPF',

    'Dados isolados por vínculo',

  ] as const,

  cadastroIntro:

    'Informe seus dados para solicitar acesso como colaborador. A equipe da instituição validará o cadastro, definirá seu perfil e liberará o primeiro login.',

  cadastroNotaInstituicao:

    'Deseja cadastrar uma nova instituição na plataforma? Entre em contato com nosso time comercial — o onboarding é conduzido pela equipe Turma360.',

  cadastroSucesso:

    'Pré-cadastro enviado com sucesso. A equipe da sua instituição ativará seu acesso e informará seu perfil. Você receberá orientações para o primeiro login.',

  loginSubtitle:

    'Informe CPF e senha. Se você atua em mais de uma instituição, selecione o vínculo desejado antes de entrar.',

  loginSemVinculo:

    'Nenhuma instituição ativa está vinculada a este CPF. Solicite acesso à secretaria ou à gestão da sua escola.',

  loginErroVinculos: 'Não foi possível carregar suas instituições. Aguarde um momento e tente novamente.',

  loginErroCredenciais:

    'CPF, senha ou instituição incorretos. Revise os dados informados e tente novamente.',

  contatoTitulo: 'Fale com a gente',

  contatoLead:

    'Tire dúvidas sobre planos, implantação e demonstração. Nossa equipe comercial atende gestores e responsáveis por instituições de ensino.',

  contatoFormLead:

    'Preencha o formulário e nossa equipe retornará por e-mail. Você também pode falar conosco pelo WhatsApp.',

  contatoRetorno: 'Respondemos em até 1 dia útil.',

  precosTitulo: 'Planos para cada fase da sua instituição',

  precosLead:

    'Comece com período de teste e evolua conforme sua operação cresce. Valores de referência — confirme condições com nosso time comercial.',

  precosAviso:

    'Valores indicativos para demonstração. Condições comerciais, descontos para múltiplas unidades e personalização são definidos com nossa equipe.',

  authAsideBullets: [

    'Um portal para todas as suas instituições',

    'Selecione o vínculo após informar o CPF',

    'Informações organizadas por instituição',

  ] as const,

  authAsideCta: 'Quer conhecer os planos? Fale conosco →',

} as const;



export const COPY_UI = {

  carregando: 'Carregando…',

  aguarde: 'Aguarde um instante.',

  portalAluno: {

    heroTitulo: 'Bem-vindo ao portal do aluno',

    heroLead:

      'Acompanhe turmas, programação e mensalidades da instituição em que você está matriculado.',

    vinculoInstituicao: (nome: string) =>

      `Você está vinculado à ${nome}. Todas as informações abaixo referem-se a essa instituição.`,

    turmasTitulo: 'Minhas turmas',

    turmasSubtitulo: 'Turmas em que você está matriculado nesta instituição',

    turmasVazioTitulo: 'Nenhuma turma vinculada',

    turmasVazioTexto:

      'Quando a secretaria ou seu professor incluir você em uma turma, ela aparecerá aqui com horários, professor e local.',

    turmasCarregando: 'Carregando suas turmas…',

    turmasErro: 'Não foi possível carregar suas turmas.',

    programacaoSubtitulo:

      'Provas, aulas, treinos e eventos publicados pela sua instituição',

    programacaoVazio:

      'Nenhuma atividade programada no momento. Quando a instituição publicar novos itens, eles aparecerão aqui em ordem cronológica.',

    dadosTitulo: 'Meus dados pessoais',

    dadosSubtitulo: 'Informações cadastrais vinculadas ao seu perfil de aluno',

    mensalidadesSubtitulo: 'Acompanhe valores, vencimentos e pagamentos da mensalidade',

    mensalidadesPagamento: 'Pagamento online',

    senhaTitulo: 'Alterar senha',

  },

  colaborador: {

    homeSubtitulo: (nome: string) =>

      `Você está vinculado à ${nome} no Turma360.`,

    masterSubtitulo: 'Central de gestão da plataforma Turma360',

  },

} as const;



export const STATS = [

  { value: '360°', label: 'Visão da operação' },

  { value: '1 login', label: 'Várias instituições' },

  { value: '100%', label: 'Foco pedagógico' },

  { value: '24/7', label: 'Sistema disponível' },

] as const;



export const SEGMENTS = [

  {

    icon: '🥋',

    title: 'Esportes e artes marciais',

    text: 'Turmas, graduações, presença e mensalidades para academias que crescem com organização.',

  },

  {

    icon: '🌐',

    title: 'Idiomas e cursos livres',

    text: 'Grade horária, matrículas e comunicação com alunos em um fluxo simples e integrado.',

  },

  {

    icon: '🎓',

    title: 'Escolas e instituições menores',

    text: 'Multi-unidade, equipe e cobrança escolar em um painel único e seguro.',

  },

] as const;



export const FEATURES = [

  {

    icon: '◉',

    title: 'Turmas e matrículas',

    text: 'Organize turmas, alunos e vínculos por instituição, com controle de vagas.',

  },

  {

    icon: '▦',

    title: 'Programação inteligente',

    text: 'Monte grades com validação de conflitos e publique para professores e alunos.',

  },

  {

    icon: '₿',

    title: 'Cobrança escolar',

    text: 'Mensalidades, inadimplência e integração com meios de pagamento.',

  },

  {

    icon: '📱',

    title: 'Portal do aluno',

    text: 'Alunos acompanham turmas, programação e mensalidades pelo celular.',

  },

  {

    icon: '👥',

    title: 'Gestão de equipe',

    text: 'Ponto, férias e entrega de holerites e recibos em PDF.',

  },

  {

    icon: '🔒',

    title: 'Multi-instituição',

    text: 'Planos por instituição, permissões granulares e operação centralizada.',

  },

] as const;



export const STEPS_SAAS = [

  {

    step: '1',

    title: 'Um endereço para todos',

    text: 'Colaboradores e alunos acessam o mesmo portal — sem links diferentes por escola.',

  },

  {

    step: '2',

    title: 'CPF e senha',

    text: 'Suas credenciais são únicas na plataforma. O sistema localiza automaticamente os vínculos ativos.',

  },

  {

    step: '3',

    title: 'Escolha onde atuar',

    text: 'Atua em mais de uma instituição? Selecione o contexto desejado antes de acessar o painel.',

  },

  {

    step: '4',

    title: 'Painel da instituição',

    text: 'Turmas, cobrança, equipe e portal do aluno — cada dado permanece isolado no vínculo escolhido.',

  },

] as const;



export const TESTIMONIALS = [

  {

    quote:

      'Organizamos turmas e mensalidades sem planilhas. A equipe adotou o sistema em poucos dias.',

    author: 'Gestora',

    role: 'Escola de idiomas',

    initials: 'GI',

  },

  {

    quote:

      'A grade horária com alerta de conflito economizou horas na montagem da programação semanal.',

    author: 'Coordenação',

    role: 'Academia de judô',

    initials: 'AJ',

  },

] as const;



export const FAQ_CONTATO = [

  {

    pergunta: 'Como contrato um plano para minha instituição?',

    resposta:

      'Envie o formulário ou fale pelo WhatsApp. Nossa equipe conduz o onboarding, cadastra a instituição e orienta o primeiro acesso da gestão.',

  },

  {

    pergunta: 'Colaboradores podem se cadastrar sozinhos?',

    resposta:

      'Sim, pelo pré-cadastro. A equipe da instituição ativa o perfil e define as permissões de cada pessoa.',

  },

  {

    pergunta: 'Preciso de um link diferente por escola?',

    resposta:

      'Não. Todos acessam o mesmo portal e escolhem a instituição após informar o CPF, quando houver mais de um vínculo.',

  },

] as const;



import { APP_NAME } from '../constants/branding';
import { isModoPlataforma, isPortalAluno, podeAtuarComoProfessor, possuiPermissao, SessaoUsuario } from './permissoes';

export interface MenuItem {
  label: string;
  path: string;
  permissao?: string;
  perfis?: string[];
  somenteMaster?: boolean;
}

export interface MenuSection {
  titulo: string;
  itens: MenuItem[];
}

export { APP_NAME };

const ITENS_COLABORADOR: MenuItem[] = [
  { label: 'Meu cadastro', path: '/arealogada/meu-perfil' },
  { label: 'Meu ponto', path: '/arealogada/colaborador/ponto' },
  { label: 'Meu holerite', path: '/arealogada/colaborador/holerite' },
  { label: 'Minhas férias', path: '/arealogada/colaborador/ferias' },
];

export function obterMenus(sessao: SessaoUsuario | null): MenuSection[] {
  if (isPortalAluno(sessao)) {
    const filtrarAluno = (itens: MenuItem[]) =>
      itens.filter((i) => !i.permissao || possuiPermissao(sessao, i.permissao));
    return [
      {
        titulo: 'Geral',
        itens: filtrarAluno([{ label: 'Início', path: '/arealogada/home' }]),
      },
      {
        titulo: 'Portal do aluno',
        itens: filtrarAluno([
          { label: 'Meus dados', path: '/arealogada/aluno/dados', permissao: 'aluno-portal:dados' },
          { label: 'Minhas turmas', path: '/arealogada/aluno/turmas', permissao: 'aluno-portal:turmas' },
          { label: 'Mensalidades', path: '/arealogada/aluno/mensalidades', permissao: 'aluno-portal:mensalidades' },
          { label: 'Alterar senha', path: '/arealogada/aluno/senha', permissao: 'aluno-portal:senha' },
          { label: 'Minha programação', path: '/arealogada/aluno/programacao', permissao: 'aluno-portal:programacao' },
        ]),
      },
    ].filter((s) => s.itens.length > 0);
  }

  const master = isModoPlataforma(sessao);
  const atuaComoProfessor = podeAtuarComoProfessor(sessao);

  const pode = (codigo?: string, perfis?: string[]) => {
    if (!sessao) return false;
    if (perfis?.length && sessao.tipoFuncionario && perfis.includes(sessao.tipoFuncionario)) {
      return true;
    }
    if (codigo) return possuiPermissao(sessao, codigo);
    return false;
  };

  const filtrarItens = (itens: MenuItem[]) =>
    itens.filter((i) => {
      if (i.somenteMaster && !master) return false;
      if (i.permissao && !pode(i.permissao, i.perfis)) return false;
      return true;
    });

  if (master) {
    const sections: MenuSection[] = [
      {
        titulo: 'Geral',
        itens: [
          { label: 'Início', path: '/arealogada/home' },
          { label: 'Dashboard administrativo', path: '/arealogada/dashboard' },
        ],
      },
      {
        titulo: 'Área do colaborador',
        itens: ITENS_COLABORADOR,
      },
      {
        titulo: 'Plataforma',
        itens: [
          { label: 'Resumo financeiro', path: '/arealogada/financeiro' },
          { label: 'Pagamentos pendentes', path: '/arealogada/financeiro/pendentes' },
          { label: 'Planos expirados', path: '/arealogada/financeiro/planos-expirados' },
        ],
      },
      {
        titulo: 'Acadêmico',
        itens: filtrarItens([
          { label: 'Consultar alunos', path: '/arealogada/alunos', permissao: 'aluno:consultar' },
          { label: 'Consultar turmas', path: '/arealogada/turmas', permissao: 'turma:consultar' },
          { label: 'Cadastrar turma', path: '/arealogada/turmas/gerenciar', permissao: 'turma:gerenciar' },
          { label: 'Matricular aluno', path: '/arealogada/matricula', permissao: 'aluno:matricular' },
          { label: 'Programação e grade', path: '/arealogada/programacao', permissao: 'programacao:consultar' },
        ]),
      },
      {
        titulo: 'Administrativo',
        itens: [
          { label: 'Consultar instituições', path: '/arealogada/instituicoes' },
          { label: 'Nova instituição (plataforma)', path: '/arealogada/cadastrar-instituicao' },
          { label: 'Funcionários', path: '/arealogada/funcionarios', permissao: 'funcionario:consultar' },
          { label: 'Ativar cadastros', path: '/arealogada/gestaoCadastro', permissao: 'funcionario:ativar' },
          { label: 'Ativar / desativar instituição', path: '/arealogada/gestaoAcademia' },
          { label: 'Auditoria', path: '/arealogada/auditoria', permissao: 'auditoria:consultar' },
        ],
      },
    ];
    return sections.filter((s) => s.itens.length > 0);
  }

  const sections: MenuSection[] = [];

  sections.push({
    titulo: 'Geral',
    itens: filtrarItens([
      { label: 'Início', path: '/arealogada/home' },
      { label: 'Dashboard administrativo', path: '/arealogada/dashboard', permissao: 'dashboard:visualizar' },
    ]),
  });

  sections.push({
    titulo: 'Área do colaborador',
    itens: ITENS_COLABORADOR,
  });

  if (pode('financeiro:visualizar')) {
    sections.push({
      titulo: 'Cobranças escolares',
      itens: filtrarItens([
        { label: 'Resumo', path: '/arealogada/financeiro' },
        { label: 'Mensalidades', path: '/arealogada/financeiro/mensalidades' },
        { label: 'Inadimplências', path: '/arealogada/financeiro/inadimplencia', permissao: 'financeiro:relatorio' },
      ]),
    });
  }

  const itensRh: MenuItem[] = [
    { label: 'Folha de ponto', path: '/arealogada/rh/folha-ponto', permissao: 'rh:folha-ponto' },
    { label: 'Férias da equipe', path: '/arealogada/rh/ferias', permissao: 'rh:ferias' },
    { label: 'Conferência mensal', path: '/arealogada/rh/conferencia-mensal', permissao: 'rh:fechamento-mensal' },
    { label: 'Holerites e recibos (PDF)', path: '/arealogada/rh/lancamento-holerite', permissao: 'rh:holerite-lancamento' },
  ];
  const rhFiltrado = filtrarItens(itensRh);
  if (rhFiltrado.length > 0) {
    sections.push({ titulo: 'Gestão de equipe', itens: rhFiltrado });
  }

  const itensProfessor: MenuItem[] = [
    { label: 'Minhas turmas', path: '/arealogada/professor/turmas' },
    { label: 'Presença', path: '/arealogada/professor/presenca', permissao: 'turma:presenca' },
  ];
  if (pode('certificado:gerar')) {
    itensProfessor.push({ label: 'Gerar certificados', path: '/arealogada/professor/certificados' });
  }
  if (atuaComoProfessor) {
    sections.push({ titulo: 'Área do professor', itens: filtrarItens(itensProfessor) });
  }

  const itensAcademico: MenuItem[] = [
    { label: 'Consultar alunos', path: '/arealogada/alunos', permissao: 'aluno:consultar' },
    ...(atuaComoProfessor ? [] : [{ label: 'Consultar turmas', path: '/arealogada/turmas', permissao: 'turma:consultar' }]),
    { label: 'Cadastrar turma', path: '/arealogada/turmas/gerenciar', permissao: 'turma:gerenciar' },
    { label: 'Matricular aluno', path: '/arealogada/matricula', permissao: 'aluno:matricular' },
    { label: 'Programação e grade', path: '/arealogada/programacao', permissao: 'programacao:consultar' },
  ];
  if (pode('certificado:gerar') && !atuaComoProfessor) {
    itensAcademico.push({ label: 'Gerar certificados', path: '/arealogada/professor/certificados' });
  }
  const academicoFiltrado = filtrarItens(itensAcademico);
  if (academicoFiltrado.length > 0) {
    sections.push({ titulo: 'Acadêmico', itens: academicoFiltrado });
  }

  const adminItens: MenuItem[] = [
    { label: 'Funcionários', path: '/arealogada/funcionarios', permissao: 'funcionario:consultar' },
    { label: 'Ativar cadastros', path: '/arealogada/gestaoCadastro', permissao: 'funcionario:ativar' },
    { label: 'Auditoria', path: '/arealogada/auditoria', permissao: 'auditoria:consultar' },
    { label: 'Plano da instituição', path: '/arealogada/plano-instituicao', permissao: 'plano:visualizar' },
  ];
  const adminFiltrado = filtrarItens(adminItens);
  if (adminFiltrado.length > 0) {
    sections.push({ titulo: 'Administrativo', itens: adminFiltrado });
  }

  return sections.filter((s) => s.itens.length > 0);
}

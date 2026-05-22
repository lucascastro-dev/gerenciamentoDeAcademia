import { APP_NAME } from '../constants/branding';

import { isPortalAluno, possuiPermissao, SessaoUsuario } from './permissoes';



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



export function obterMenus(sessao: SessaoUsuario | null): MenuSection[] {
  if (isPortalAluno(sessao)) {
    const filtrarAluno = (itens: MenuItem[]) =>
      itens.filter((i) => !i.permissao || possuiPermissao(sessao, i.permissao));
    return [
      {
        titulo: 'Geral',
        itens: filtrarAluno([
          { label: 'Início', path: '/arealogada/home' },
        ]),
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

  const master = !!sessao?.usuarioMaster;

  const ehProfessor = sessao?.tipoFuncionario === 'PROFESSOR';



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



  const sections: MenuSection[] = [];



  sections.push({

    titulo: 'Geral',

    itens: filtrarItens([

      { label: 'Início', path: '/arealogada/home' },

      { label: 'Meu cadastro', path: '/arealogada/meu-perfil' },

      { label: 'Dashboard administrativo', path: '/arealogada/dashboard', permissao: 'dashboard:visualizar' },

    ]),

  });



  if (pode('financeiro:visualizar')) {

    sections.push({

      titulo: 'Financeiro',

      itens: filtrarItens([

        { label: 'Dashboard financeiro', path: '/arealogada/financeiro' },

        { label: 'Mensalidades', path: '/arealogada/financeiro/mensalidades' },

        { label: 'Inadimplência', path: '/arealogada/financeiro/inadimplencia', permissao: 'financeiro:relatorio' },

      ]),

    });

  }



  const itensProfessor: MenuItem[] = [

    { label: 'Minhas turmas', path: '/arealogada/professor/turmas' },

    { label: 'Alunos das turmas', path: '/arealogada/professor/alunos' },

    { label: 'Presença (em breve)', path: '/arealogada/professor/presenca', permissao: 'turma:presenca' },

  ];

  if (pode('certificado:gerar') && ehProfessor) {

    itensProfessor.splice(2, 0, { label: 'Gerar certificados', path: '/arealogada/professor/certificados' });

  }



  if (pode('turma:consultar', ['PROFESSOR'])) {

    sections.push({ titulo: 'Área do professor', itens: filtrarItens(itensProfessor) });

  }



  const itensAcademico: MenuItem[] = [

      { label: 'Consultar alunos', path: '/arealogada/alunos', permissao: 'aluno:consultar' },

    { label: 'Consultar turmas', path: '/arealogada/turmas', permissao: 'turma:consultar' },

    { label: 'Cadastrar turma', path: '/arealogada/turmas/gerenciar', permissao: 'turma:gerenciar' },

    { label: 'Matricular aluno', path: '/arealogada/matricula', permissao: 'aluno:matricular' },

    { label: 'Programação e grade', path: '/arealogada/programacao', permissao: 'programacao:consultar' },

  ];

  if (pode('certificado:gerar') && !ehProfessor) {

    itensAcademico.push({ label: 'Gerar certificados', path: '/arealogada/professor/certificados' });

  }



  sections.push({ titulo: 'Acadêmico', itens: filtrarItens(itensAcademico) });



  const adminItens: MenuItem[] = [

    { label: 'Consultar instituições', path: '/arealogada/instituicoes', permissao: 'academia:consultar' },

    { label: 'Nova instituição (plataforma)', path: '/arealogada/cadastrar-instituicao', somenteMaster: true },

    { label: 'Funcionários', path: '/arealogada/funcionarios', permissao: 'funcionario:consultar' },

    { label: 'Ativar cadastros', path: '/arealogada/gestaoCadastro', permissao: 'funcionario:ativar' },

    { label: 'Ativar / desativar unidade', path: '/arealogada/gestaoAcademia', permissao: 'academia:ativar-inativar' },

    { label: 'Auditoria', path: '/arealogada/auditoria', permissao: 'auditoria:consultar' },

    { label: 'Plano da plataforma', path: '/arealogada/plano-instituicao', permissao: 'plano:visualizar' },

  ];



  const adminFiltrado = filtrarItens(adminItens);

  if (adminFiltrado.length > 0) {

    sections.push({ titulo: 'Administrativo', itens: adminFiltrado });

  }



  return sections.filter((s) => s.itens.length > 0);

}



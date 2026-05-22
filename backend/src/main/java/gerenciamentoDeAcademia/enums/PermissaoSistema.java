package gerenciamentoDeAcademia.enums;

import lombok.Getter;

/**
 * Permissões granulares do sistema. Perfis master recebem todas automaticamente.
 */
@Getter
public enum PermissaoSistema {
    DASHBOARD_VISUALIZAR("dashboard:visualizar"),

    ACADEMIA_CONSULTAR("academia:consultar"),
    ACADEMIA_GERENCIAR("academia:gerenciar"),
    ACADEMIA_ATIVAR_INATIVAR("academia:ativar-inativar"),

    FUNCIONARIO_CONSULTAR("funcionario:consultar"),
    FUNCIONARIO_CADASTRAR("funcionario:cadastrar"),
    FUNCIONARIO_EDITAR("funcionario:editar"),
    FUNCIONARIO_EXCLUIR("funcionario:excluir"),
    FUNCIONARIO_ATIVAR("funcionario:ativar"),
    FUNCIONARIO_AUDITORIA("funcionario:auditoria"),

    ALUNO_CONSULTAR("aluno:consultar"),
    ALUNO_CADASTRAR("aluno:cadastrar"),
    ALUNO_EDITAR("aluno:editar"),
    ALUNO_MATRICULAR("aluno:matricular"),
    ALUNO_DESMATRICULAR("aluno:desmatricular"),

    TURMA_CONSULTAR("turma:consultar"),
    TURMA_GERENCIAR("turma:gerenciar"),
    TURMA_PRESENCA("turma:presenca"),
    TURMA_AVALIACAO("turma:avaliacao"),

    FINANCEIRO_VISUALIZAR("financeiro:visualizar"),
    FINANCEIRO_COBRANCA("financeiro:cobranca"),
    FINANCEIRO_RELATORIO("financeiro:relatorio"),

    CERTIFICADO_GERAR("certificado:gerar"),

    AUDITORIA_CONSULTAR("auditoria:consultar"),
    USUARIO_PERMISSOES("usuario:permissoes"),

    PLANO_INSTITUICAO_VISUALIZAR("plano:visualizar"),
    PLANO_INSTITUICAO_GERENCIAR("plano:gerenciar"),

    ALUNO_PORTAL_DADOS("aluno-portal:dados"),
    ALUNO_PORTAL_TURMAS("aluno-portal:turmas"),
    ALUNO_PORTAL_MENSALIDADES("aluno-portal:mensalidades"),
    ALUNO_PORTAL_PAGAMENTO("aluno-portal:pagamento"),
    ALUNO_PORTAL_SENHA("aluno-portal:senha"),
    ALUNO_PORTAL_PROGRAMACAO("aluno-portal:programacao"),

    PROGRAMACAO_CONSULTAR("programacao:consultar"),
    PROGRAMACAO_GERENCIAR("programacao:gerenciar");

    private final String codigo;

    PermissaoSistema(String codigo) {
        this.codigo = codigo;
    }
}

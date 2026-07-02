package gerenciamentoDeAcademia.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tipos de colaboradores da instituição com segregação de funções (SoD).
 * Master da plataforma é definido por CPF em {@code app.master.cpf} (ver {@code ServicoMasterPlataforma}).
 */
@Getter
public enum TipoFuncionario {
    OPERADOR_PLATAFORMA(false, "Usuário Master"),
    DIRETOR(false, "Diretor"),
    FINANCEIRO(false, "Financeiro"),
    RH(false, "Recursos Humanos"),
    TI(false, "Tecnologia da Informação"),
    ADMINISTRADOR(false, "Administrador"),
    RECEPCIONISTA(false, "Recepcionista"),
    SERVICOS_GERAIS(false, "Serviços Gerais"),
    PROFESSOR(false, "Professor"),
    ESTAGIARIO(false, "Estagiário"),
    TERCEIRIZADO(false, "Terceirizado");

    private final boolean usuarioMaster;
    private final String descricao;

    TipoFuncionario(boolean usuarioMaster, String descricao) {
        this.usuarioMaster = usuarioMaster;
        this.descricao = descricao;
    }

    public boolean isUsuarioMaster() {
        return usuarioMaster;
    }

    /** Diretor, administrador e professor podem ser vinculados a turmas e usar a área pedagógica. */
    public boolean podeAtuarComoProfessor() {
        return this == PROFESSOR || this == DIRETOR || this == ADMINISTRADOR;
    }

    public Set<PermissaoSistema> permissoesPadrao() {
        if (usuarioMaster) {
            return EnumSet.allOf(PermissaoSistema.class);
        }
        return switch (this) {
            case ADMINISTRADOR -> EnumSet.of(
                    PermissaoSistema.DASHBOARD_VISUALIZAR,
                    PermissaoSistema.FINANCEIRO_VISUALIZAR,
                    PermissaoSistema.FINANCEIRO_COBRANCA,
                    PermissaoSistema.FINANCEIRO_RELATORIO,
                    PermissaoSistema.FUNCIONARIO_CONSULTAR,
                    PermissaoSistema.FUNCIONARIO_CADASTRAR,
                    PermissaoSistema.FUNCIONARIO_EDITAR,
                    PermissaoSistema.FUNCIONARIO_ATIVAR,
                    PermissaoSistema.ALUNO_CONSULTAR,
                    PermissaoSistema.ALUNO_CADASTRAR,
                    PermissaoSistema.ALUNO_EDITAR,
                    PermissaoSistema.ALUNO_MATRICULAR,
                    PermissaoSistema.ALUNO_DESMATRICULAR,
                    PermissaoSistema.TURMA_CONSULTAR,
                    PermissaoSistema.TURMA_GERENCIAR,
                    PermissaoSistema.TURMA_GERENCIAR_ALUNOS,
                    PermissaoSistema.TURMA_PRESENCA,
                    PermissaoSistema.TURMA_AVALIACAO,
                    PermissaoSistema.CERTIFICADO_GERAR,
                    PermissaoSistema.PLANO_INSTITUICAO_VISUALIZAR,
                    PermissaoSistema.PROGRAMACAO_CONSULTAR,
                    PermissaoSistema.PROGRAMACAO_GERENCIAR,
                    PermissaoSistema.PROGRAMACAO_GERENCIAR_ITENS,
                    PermissaoSistema.RH_FOLHA_PONTO,
                    PermissaoSistema.RH_FECHAMENTO_MENSAL,
                    PermissaoSistema.RH_FERIAS,
                    PermissaoSistema.RH_FERIAS_GERENCIAR,
                    PermissaoSistema.RH_LANCAMENTO_HOLERITE
            );
            case TI -> EnumSet.of(
                    PermissaoSistema.DASHBOARD_VISUALIZAR,
                    PermissaoSistema.ACADEMIA_CONSULTAR,
                    PermissaoSistema.FUNCIONARIO_CONSULTAR,
                    PermissaoSistema.FUNCIONARIO_EDITAR,
                    PermissaoSistema.ALUNO_CONSULTAR,
                    PermissaoSistema.TURMA_CONSULTAR,
                    PermissaoSistema.TURMA_GERENCIAR,
                    PermissaoSistema.PROGRAMACAO_CONSULTAR,
                    PermissaoSistema.PROGRAMACAO_GERENCIAR,
                    PermissaoSistema.AUDITORIA_CONSULTAR,
                    PermissaoSistema.PLANO_INSTITUICAO_VISUALIZAR
            );
            case FINANCEIRO -> EnumSet.of(
                    PermissaoSistema.DASHBOARD_VISUALIZAR,
                    PermissaoSistema.FINANCEIRO_VISUALIZAR,
                    PermissaoSistema.FINANCEIRO_COBRANCA,
                    PermissaoSistema.FINANCEIRO_RELATORIO,
                    PermissaoSistema.ALUNO_CONSULTAR,
                    PermissaoSistema.FUNCIONARIO_CONSULTAR,
                    PermissaoSistema.PLANO_INSTITUICAO_VISUALIZAR
            );
            case RH -> EnumSet.of(
                    PermissaoSistema.DASHBOARD_VISUALIZAR,
                    PermissaoSistema.FUNCIONARIO_CONSULTAR,
                    PermissaoSistema.FUNCIONARIO_CADASTRAR,
                    PermissaoSistema.FUNCIONARIO_EDITAR,
                    PermissaoSistema.FUNCIONARIO_ATIVAR,
                    PermissaoSistema.RH_FOLHA_PONTO,
                    PermissaoSistema.RH_FECHAMENTO_MENSAL,
                    PermissaoSistema.RH_LANCAMENTO_HOLERITE,
                    PermissaoSistema.RH_FERIAS,
                    PermissaoSistema.RH_FERIAS_GERENCIAR
            );
            case RECEPCIONISTA -> EnumSet.of(
                    PermissaoSistema.ALUNO_CONSULTAR,
                    PermissaoSistema.ALUNO_CADASTRAR,
                    PermissaoSistema.ALUNO_EDITAR,
                    PermissaoSistema.ALUNO_MATRICULAR,
                    PermissaoSistema.FINANCEIRO_COBRANCA,
                    PermissaoSistema.TURMA_CONSULTAR,
                    PermissaoSistema.PROGRAMACAO_CONSULTAR,
                    PermissaoSistema.PROGRAMACAO_GERENCIAR
            );
            case PROFESSOR -> EnumSet.of(
                    PermissaoSistema.TURMA_CONSULTAR,
                    PermissaoSistema.TURMA_GERENCIAR_ALUNOS,
                    PermissaoSistema.TURMA_PRESENCA,
                    PermissaoSistema.TURMA_AVALIACAO,
                    PermissaoSistema.CERTIFICADO_GERAR,
                    PermissaoSistema.ALUNO_CONSULTAR,
                    PermissaoSistema.PROGRAMACAO_CONSULTAR,
                    PermissaoSistema.PROGRAMACAO_GERENCIAR_ITENS
            );
            case DIRETOR -> {
                EnumSet<PermissaoSistema> diretor = EnumSet.of(
                        PermissaoSistema.DASHBOARD_VISUALIZAR,
                        PermissaoSistema.FINANCEIRO_VISUALIZAR,
                        PermissaoSistema.FINANCEIRO_RELATORIO,
                        PermissaoSistema.FUNCIONARIO_CONSULTAR,
                        PermissaoSistema.ALUNO_CONSULTAR,
                        PermissaoSistema.ALUNO_CADASTRAR,
                        PermissaoSistema.ALUNO_EDITAR,
                        PermissaoSistema.TURMA_CONSULTAR,
                        PermissaoSistema.TURMA_GERENCIAR,
                        PermissaoSistema.PLANO_INSTITUICAO_VISUALIZAR,
                        PermissaoSistema.PROGRAMACAO_CONSULTAR,
                        PermissaoSistema.PROGRAMACAO_GERENCIAR,
                        PermissaoSistema.TURMA_GERENCIAR_ALUNOS,
                        PermissaoSistema.TURMA_PRESENCA,
                        PermissaoSistema.TURMA_AVALIACAO,
                        PermissaoSistema.CERTIFICADO_GERAR,
                        PermissaoSistema.PROGRAMACAO_GERENCIAR_ITENS
                );
                yield diretor;
            }
            case ESTAGIARIO -> EnumSet.of(
                    PermissaoSistema.ALUNO_CONSULTAR,
                    PermissaoSistema.TURMA_CONSULTAR,
                    PermissaoSistema.PROGRAMACAO_CONSULTAR
            );
            case SERVICOS_GERAIS -> EnumSet.of(
                    PermissaoSistema.ALUNO_CONSULTAR
            );
            case TERCEIRIZADO -> EnumSet.noneOf(PermissaoSistema.class);
            default -> EnumSet.noneOf(PermissaoSistema.class);
        };
    }

    /**
     * Permissões do terceirizado conforme área vinculada no cadastro.
     */
    public static Set<PermissaoSistema> permissoesTerceirizado(AreaTerceirizado area) {
        if (area == null) {
            return EnumSet.noneOf(PermissaoSistema.class);
        }
        return switch (area) {
            case RH -> EnumSet.of(
                    PermissaoSistema.FUNCIONARIO_CONSULTAR,
                    PermissaoSistema.ALUNO_CONSULTAR
            );
            case PROFESSOR_SUBSTITUTO -> EnumSet.of(
                    PermissaoSistema.TURMA_CONSULTAR,
                    PermissaoSistema.TURMA_PRESENCA,
                    PermissaoSistema.ALUNO_CONSULTAR,
                    PermissaoSistema.CERTIFICADO_GERAR
            );
            case TI -> EnumSet.of(
                    PermissaoSistema.DASHBOARD_VISUALIZAR,
                    PermissaoSistema.ACADEMIA_CONSULTAR,
                    PermissaoSistema.FUNCIONARIO_CONSULTAR
            );
        };
    }

    public static Set<String> codigosPermissao(TipoFuncionario tipo, AreaTerceirizado areaTerceirizado) {
        if (tipo == null) {
            return Set.of();
        }
        Set<PermissaoSistema> base = tipo == TipoFuncionario.TERCEIRIZADO
                ? permissoesTerceirizado(areaTerceirizado)
                : tipo.permissoesPadrao();
        return base.stream()
                .map(PermissaoSistema::getCodigo)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static TipoFuncionario fromCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            return null;
        }
        String normalizado = cargo.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("Ç", "C")
                .replace("Ã", "A");
        return Arrays.stream(values())
                .filter(t -> t.name().equals(normalizado)
                        || t.descricao.equalsIgnoreCase(cargo.trim())
                        || t.name().replace("_", " ").equalsIgnoreCase(cargo.trim()))
                .findFirst()
                .orElse(null);
    }
}

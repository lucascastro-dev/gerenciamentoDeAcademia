package gerenciamentoDeAcademia.enums;

import lombok.Getter;

/**
 * Área de atuação do colaborador terceirizado — define escopo limitado de permissões.
 */
@Getter
public enum AreaTerceirizado {
    RH("Apoio RH"),
    PROFESSOR_SUBSTITUTO("Professor substituto"),
    TI("Apoio TI");

    private final String descricao;

    AreaTerceirizado(String descricao) {
        this.descricao = descricao;
    }
}

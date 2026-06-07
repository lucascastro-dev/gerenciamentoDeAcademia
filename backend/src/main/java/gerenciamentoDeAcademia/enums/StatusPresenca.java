package gerenciamentoDeAcademia.enums;

import lombok.Getter;

@Getter
public enum StatusPresenca {
    P("Presente"),
    F("Falta"),
    J("Falta justificada"),
    A("Atraso");

    private final String descricao;

    StatusPresenca(String descricao) {
        this.descricao = descricao;
    }
}

package gerenciamentoDeAcademia.enums;

import lombok.Getter;

@Getter
public enum StatusSolicitacaoFerias {
    PENDENTE("Pendente"),
    APROVADO("Aprovado"),
    REJEITADO("Rejeitado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusSolicitacaoFerias(String descricao) {
        this.descricao = descricao;
    }
}

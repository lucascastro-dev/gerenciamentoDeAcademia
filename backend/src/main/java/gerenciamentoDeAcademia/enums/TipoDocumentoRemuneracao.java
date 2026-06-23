package gerenciamentoDeAcademia.enums;

import lombok.Getter;

@Getter
public enum TipoDocumentoRemuneracao {
    HOLERITE("Holerite"),
    RECIBO("Recibo de pagamento"),
    INFORME("Informe de rendimentos");

    private final String descricao;

    TipoDocumentoRemuneracao(String descricao) {
        this.descricao = descricao;
    }
}

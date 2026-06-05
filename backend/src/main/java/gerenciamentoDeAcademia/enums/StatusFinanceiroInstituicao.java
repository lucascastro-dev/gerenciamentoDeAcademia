package gerenciamentoDeAcademia.enums;

import lombok.Getter;

/**
 * Situação do pagamento do plano SaaS da instituição (controle manual até integração com gateway).
 */
@Getter
public enum StatusFinanceiroInstituicao {
    NAO_APLICAVEL("Não aplicável"),
    PENDENTE_PAGAMENTO("Pagamento pendente"),
    PAGAMENTO_CONFIRMADO("Pagamento confirmado");

    private final String descricao;

    StatusFinanceiroInstituicao(String descricao) {
        this.descricao = descricao;
    }
}

package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtualizarStatusFinanceiroRequest {
    private String cnpj;
    private StatusFinanceiroInstituicao statusFinanceiro;
}

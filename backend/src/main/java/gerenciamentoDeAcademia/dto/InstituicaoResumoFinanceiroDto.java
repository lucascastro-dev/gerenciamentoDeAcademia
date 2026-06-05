package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;

public record InstituicaoResumoFinanceiroDto(
        Long id,
        String razaoSocial,
        String cnpj,
        StatusFinanceiroInstituicao statusFinanceiro,
        PlanoInstituicaoTipo plano,
        boolean planoVigente
) {
}

package gerenciamentoDeAcademia.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardFinanceiroPlataformaDto(
        long instituicoesPagamentoPendente,
        long instituicoesPlanoVencido,
        long instituicoesComPlanoVigente,
        BigDecimal receitaPrevistaConfirmada,
        BigDecimal receitaPrevistaAguardandoPagamento,
        List<InstituicaoResumoFinanceiroDto> destaquesPendentes,
        List<InstituicaoResumoFinanceiroDto> destaquesPlanoExpirado
) {
}

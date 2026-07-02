package gerenciamentoDeAcademia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MensalidadeHistoricoItemDto(
        int mes,
        int ano,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        String status,
        String statusDescricao,
        BigDecimal valor,
        Long cobrancaId,
        boolean podeGerarCobranca
) {}

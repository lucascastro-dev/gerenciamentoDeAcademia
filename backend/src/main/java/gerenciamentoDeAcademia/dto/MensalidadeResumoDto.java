package gerenciamentoDeAcademia.dto;

import java.time.LocalDate;

public record MensalidadeResumoDto(
        String cpf,
        String nome,
        Double valorMensalidade,
        Integer diaVencimento,
        boolean inadimplente,
        LocalDate dataUltimoPagamento
) {
}

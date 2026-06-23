package gerenciamentoDeAcademia.dto;

import java.time.LocalDateTime;

public record StatusPontoHojeDto(
        String proximaAcao,
        LocalDateTime horaEntrada,
        LocalDateTime horaSaida,
        String mensagem
) {
}

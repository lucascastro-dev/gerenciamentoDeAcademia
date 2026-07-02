package gerenciamentoDeAcademia.dto;

import java.time.LocalDate;

public record PeriodoAquisitivoFeriasDto(
        LocalDate inicio,
        LocalDate fim,
        int diasDireito,
        int diasUtilizados,
        int diasPendentes,
        int diasDisponiveis,
        String situacao) {
}

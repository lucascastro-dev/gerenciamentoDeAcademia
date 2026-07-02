package gerenciamentoDeAcademia.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SolicitarAjustePontoFormDto(
        LocalDate dataRegistro,
        LocalTime horaEntradaProposta,
        LocalTime horaSaidaProposta,
        String justificativa
) {}

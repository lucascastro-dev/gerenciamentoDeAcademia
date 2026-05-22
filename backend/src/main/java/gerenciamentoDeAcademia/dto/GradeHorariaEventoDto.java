package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.TipoItemProgramacao;

import java.time.LocalDate;
import java.time.LocalTime;

public record GradeHorariaEventoDto(
        String origem,
        Long referenciaId,
        String titulo,
        String subtitulo,
        TipoItemProgramacao tipoProgramacao,
        String modalidade,
        String sala,
        String diaSemana,
        LocalDate data,
        LocalTime horaInicio,
        LocalTime horaFim,
        boolean conflito
) {
}

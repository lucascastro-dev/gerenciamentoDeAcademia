package gerenciamentoDeAcademia.dto;

import java.util.List;

public record ResumoPontoMensalDto(
        Integer mesCompetencia,
        Integer anoCompetencia,
        List<RegistroDiaPontoDto> registros,
        Long totalMinutosTrabalhados,
        String totalHorasFormatadas,
        Integer diasComRegistroCompleto
) {
}

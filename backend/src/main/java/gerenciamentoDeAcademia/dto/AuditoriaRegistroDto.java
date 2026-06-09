package gerenciamentoDeAcademia.dto;

import java.time.LocalDateTime;

public record AuditoriaRegistroDto(
        Long id,
        String ajuste,
        LocalDateTime dataHora,
        String usuarioLogin,
        String entidade,
        String referencia,
        String motivo
) {
}

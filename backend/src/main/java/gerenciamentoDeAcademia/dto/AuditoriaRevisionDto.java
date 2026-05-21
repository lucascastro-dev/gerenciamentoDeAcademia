package gerenciamentoDeAcademia.dto;

import java.time.Instant;

public record AuditoriaRevisionDto(
        Long revisionNumber,
        Instant revisionDate,
        String cpf,
        String nome,
        String tipoFuncionario,
        Boolean cadastroAtivo,
        String enderecoResumo
) {
}

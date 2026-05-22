package gerenciamentoDeAcademia.dto;

public record ConflitoHorarioDto(
        String mensagem,
        String eventoA,
        String eventoB,
        String sala
) {
}

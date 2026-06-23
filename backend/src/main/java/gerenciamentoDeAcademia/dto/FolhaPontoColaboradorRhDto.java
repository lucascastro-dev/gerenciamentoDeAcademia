package gerenciamentoDeAcademia.dto;

public record FolhaPontoColaboradorRhDto(
        String cpf,
        String nome,
        String cargo,
        Integer diasTrabalhados,
        Long minutosTrabalhados,
        String horasFormatadas,
        boolean possuiRegistroAberto
) {
}

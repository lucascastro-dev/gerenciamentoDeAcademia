package gerenciamentoDeAcademia.dto;

public record DashboardPlataformaDto(
        long instituicoesCadastradas,
        long instituicoesAtivas,
        long instituicoesInativas,
        long colaboradoresAtivos,
        long colaboradoresPendentesAtivacao,
        long turmasCadastradas,
        long planosVencidos
) {
}

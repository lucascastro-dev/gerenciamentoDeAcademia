package gerenciamentoDeAcademia.dto;

public record DashboardResumoDto(
        long totalAlunos,
        long funcionariosAtivos,
        long totalTurmas
) {
}

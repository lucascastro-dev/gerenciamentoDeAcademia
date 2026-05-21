package gerenciamentoDeAcademia.dto;

public record DashboardResumoDto(
        long totalAlunos,
        long totalFuncionarios,
        long funcionariosAtivos,
        long totalTurmas
) {
}

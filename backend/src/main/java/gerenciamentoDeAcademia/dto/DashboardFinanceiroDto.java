package gerenciamentoDeAcademia.dto;

import java.util.List;

public record DashboardFinanceiroDto(
        long totalAlunos,
        double receitaMensalPrevista,
        long alunosInadimplentes,
        double valorInadimplente,
        List<MensalidadeResumoDto> proximosVencimentos
) {
}

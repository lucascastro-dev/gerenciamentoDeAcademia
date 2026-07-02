package gerenciamentoDeAcademia.dto;

import java.util.List;

public record ResumoFeriasColaboradorDto(
        int diasDisponiveisTotal,
        int diasAprovadosTotal,
        int diasPendentesTotal,
        List<PeriodoAquisitivoFeriasDto> periodos,
        List<SolicitacaoFeriasDto> solicitacoes) {
}

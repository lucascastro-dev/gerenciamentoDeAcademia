package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.SolicitacaoFerias;
import gerenciamentoDeAcademia.enums.StatusSolicitacaoFerias;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitacaoFeriasDto(
        Long id,
        String cpfColaborador,
        String nomeColaborador,
        LocalDate dataInicio,
        LocalDate dataFim,
        int diasSolicitados,
        LocalDate inicioPeriodoAquisitivo,
        LocalDate fimPeriodoAquisitivo,
        StatusSolicitacaoFerias status,
        String statusDescricao,
        LocalDateTime criadoEm,
        String observacaoRh) {

    public static SolicitacaoFeriasDto of(SolicitacaoFerias s) {
        return new SolicitacaoFeriasDto(
                s.getId(),
                s.getCpfColaborador(),
                s.getNomeColaborador(),
                s.getDataInicio(),
                s.getDataFim(),
                s.getDiasSolicitados(),
                s.getInicioPeriodoAquisitivo(),
                s.getFimPeriodoAquisitivo(),
                s.getStatus(),
                s.getStatus().getDescricao(),
                s.getCriadoEm(),
                s.getObservacaoRh());
    }
}

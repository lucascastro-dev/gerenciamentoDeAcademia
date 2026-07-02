package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.SolicitacaoAjustePonto;
import gerenciamentoDeAcademia.enums.StatusSolicitacaoAjustePonto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitacaoAjustePontoDto(
        Long id,
        String cpfColaborador,
        String nomeColaborador,
        LocalDate dataRegistro,
        LocalDateTime horaEntradaAtual,
        LocalDateTime horaSaidaAtual,
        LocalDateTime horaEntradaProposta,
        LocalDateTime horaSaidaProposta,
        String justificativa,
        StatusSolicitacaoAjustePonto status,
        LocalDateTime criadoEm,
        LocalDateTime decididoEm,
        String observacaoGestor
) {
    public static SolicitacaoAjustePontoDto of(SolicitacaoAjustePonto s) {
        return new SolicitacaoAjustePontoDto(
                s.getId(),
                s.getCpfColaborador(),
                s.getNomeColaborador(),
                s.getDataRegistro(),
                s.getHoraEntradaAtual(),
                s.getHoraSaidaAtual(),
                s.getHoraEntradaProposta(),
                s.getHoraSaidaProposta(),
                s.getJustificativa(),
                s.getStatus(),
                s.getCriadoEm(),
                s.getDecididoEm(),
                s.getObservacaoGestor());
    }
}

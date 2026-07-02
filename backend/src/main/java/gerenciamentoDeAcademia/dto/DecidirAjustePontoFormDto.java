package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.StatusSolicitacaoAjustePonto;

public record DecidirAjustePontoFormDto(
        StatusSolicitacaoAjustePonto status,
        String observacaoGestor
) {}

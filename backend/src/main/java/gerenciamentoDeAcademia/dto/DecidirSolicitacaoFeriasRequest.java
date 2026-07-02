package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.StatusSolicitacaoFerias;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class DecidirSolicitacaoFeriasRequest {

    @NotNull(message = "Informe a decisão.")
    private StatusSolicitacaoFerias status;

    @Size(max = 500)
    private String observacaoRh;
}

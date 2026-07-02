package gerenciamentoDeAcademia.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class CriarSolicitacaoFeriasRequest {

    @NotNull(message = "Informe a data de início.")
    private LocalDate dataInicio;

    @NotNull(message = "Informe a data de fim.")
    private LocalDate dataFim;
}

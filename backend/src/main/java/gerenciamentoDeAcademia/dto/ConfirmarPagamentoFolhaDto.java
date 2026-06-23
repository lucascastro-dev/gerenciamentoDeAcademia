package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ConfirmarPagamentoFolhaDto {
    private String cpfColaborador;
    private Integer mesCompetencia;
    private Integer anoCompetencia;
    private BigDecimal valorBruto;
    private BigDecimal valorLiquido;
}

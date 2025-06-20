package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlunoDto extends PessoaDto {
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String faixa;
    private String tamanhoFaixa;
}
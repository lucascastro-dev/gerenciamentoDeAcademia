package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioDto extends PessoaDto {
    private String cargo;
    private String especializacao;
    private Boolean permitirGerenciarFuncoes;

}
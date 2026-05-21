package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtivacaoFuncionarioDto {
    private TipoFuncionario tipoFuncionario;
    private AreaTerceirizado areaTerceirizado;
    private String especializacao;
}

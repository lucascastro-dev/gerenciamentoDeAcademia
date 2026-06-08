package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Funcionario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioConsultaCompletaDto extends FuncionarioDto {
    private List<FuncionarioVinculoInstituicaoDto> vinculos = new ArrayList<>();

    public FuncionarioConsultaCompletaDto(Funcionario funcionario) {
        super(funcionario);
    }
}

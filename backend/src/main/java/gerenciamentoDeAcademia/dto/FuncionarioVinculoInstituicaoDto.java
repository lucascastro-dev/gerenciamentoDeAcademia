package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.AreaTerceirizado;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioVinculoInstituicaoDto {
    private Long vinculoId;
    private Long instituicaoId;
    private String razaoSocial;
    private TipoFuncionario tipoFuncionario;
    private AreaTerceirizado areaTerceirizado;
    private String especializacao;
    private String cargo;

    public static FuncionarioVinculoInstituicaoDto de(VinculoFuncionarioInstituicao vinculo) {
        FuncionarioVinculoInstituicaoDto dto = new FuncionarioVinculoInstituicaoDto();
        dto.setVinculoId(vinculo.getId());
        dto.setInstituicaoId(vinculo.getInstituicao().getId());
        dto.setRazaoSocial(vinculo.getInstituicao().getRazaoSocial());
        dto.setTipoFuncionario(vinculo.getTipoFuncionario());
        dto.setAreaTerceirizado(vinculo.getAreaTerceirizado());
        dto.setEspecializacao(vinculo.getEspecializacao());
        dto.setCargo(vinculo.descricaoCargo());
        return dto;
    }
}

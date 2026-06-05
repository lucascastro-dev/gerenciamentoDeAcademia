package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtivarInstituicaoRequest {
    private String cnpj;
    private String cpfAdministrador;
    private PlanoInstituicaoTipo plano;
}

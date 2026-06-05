package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AtualizarPlanoInstituicaoRequest {
    private String cnpj;
    private PlanoInstituicaoTipo plano;
}

package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlterarSenhaDto {
    private String senhaAtual;
    private String senhaNova;
}

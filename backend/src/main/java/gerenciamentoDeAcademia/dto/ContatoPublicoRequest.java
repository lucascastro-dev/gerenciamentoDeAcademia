package gerenciamentoDeAcademia.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ContatoPublicoRequest {

    @NotBlank(message = "Informe seu nome.")
    @Size(max = 120)
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "E-mail inválido.")
    @Size(max = 180)
    private String email;

    @Size(max = 30)
    private String telefone;

    @Size(max = 180)
    private String instituicao;

    @NotBlank(message = "Escreva sua mensagem.")
    @Size(max = 4000)
    private String mensagem;
}

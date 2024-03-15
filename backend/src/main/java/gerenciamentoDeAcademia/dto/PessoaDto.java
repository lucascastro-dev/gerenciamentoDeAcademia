package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PessoaDto {
    private String nome;
    private String rg;
    private String cpf;
    private LocalDate dataDeNascimento;
    private String endereco;
    private String telefone;

}
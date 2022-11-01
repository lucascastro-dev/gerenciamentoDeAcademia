package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioDto extends PessoaDto {
    private String cargo;
    private String especializacao;

    public FuncionarioDto(String nome, String rg, String cpf, LocalDate dataDeNascimento, String endereco, String telefone, String cargo, String especializacao) {
        super(nome, rg, cpf, dataDeNascimento, endereco, telefone);
        this.cargo = cargo;
        this.especializacao = especializacao;
    }
}
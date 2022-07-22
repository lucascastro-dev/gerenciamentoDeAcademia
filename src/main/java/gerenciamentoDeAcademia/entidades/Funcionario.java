package gerenciamentoDeAcademia.entidades;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Funcionario extends Pessoa {
    private String cargo;
    private String especializacao;

    public Funcionario(String nome, String rg, String cpf, LocalDate dataDeNascimento, String endereco, String telefone, String cargo, String especializacao) {
        super(nome, rg, cpf, dataDeNascimento, endereco, telefone);
        this.cargo = cargo;
        this.especializacao = especializacao;
    }
}
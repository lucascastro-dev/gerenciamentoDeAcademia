package gerenciamentoDeAcademia.Entidades;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Funcionario extends Pessoa {

    private String cargo;
    private String especializacao;

    public Funcionario(String nome, String rg, String cpf, LocalDateTime dataDeNascimento, String endereco, String telefone, String cargo, String especializacao) {
        super(nome, rg, cpf, dataDeNascimento, endereco, telefone);

        if (cargo == null)
            throw new RuntimeException("Cargo é obrigatório!");

        if (cargo == "Professor")
            if (especializacao == null)
                throw new RuntimeException("Especialização é obrigatória para professor!");

        this.cargo = cargo;
        this.especializacao = especializacao;
    }
}
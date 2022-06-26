package gerenciamentoDeAcademia.Entidades;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Pessoa {
    private String nome;
    private String rg;
    private String cpf;
    private LocalDateTime dataDeNascimento;
    private String endereco;
    private String telefone;

    public Pessoa(String nome, String rg, String cpf, LocalDateTime dataDeNascimento, String endereco, String telefone) {

        if (nome == null)
            throw new RuntimeException("Nome é obrigatório!");

        if (rg == null)
            throw new RuntimeException("RG é obrigatório!");

        if (cpf == null)
            throw new RuntimeException("CPF é obrigatório!");

        if (dataDeNascimento == null)
            throw new RuntimeException("Data de nascimento é obrigatória!");

        if (endereco == null)
            throw new RuntimeException("Endereço é obrigatório!");

        if (telefone == null)
            throw new RuntimeException("Telefone é obrigatório!");

        this.nome = nome;
        this.rg = rg;
        this.cpf = cpf;
        this.dataDeNascimento = dataDeNascimento;
        this.endereco = endereco;
        this.telefone = telefone;
    }
}
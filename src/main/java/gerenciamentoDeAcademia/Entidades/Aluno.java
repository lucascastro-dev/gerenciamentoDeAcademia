package gerenciamentoDeAcademia.Entidades;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Aluno extends Pessoa {
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private String nomeResponsavel;
    private String telefoneResponsavel;

    public Aluno(String nome, String rg, String cpf, LocalDateTime dataDeNascimento, String endereco, String telefone, Double valorMensalidade, Integer diaVencimentoMensalidade, String nomeResponsavel, String telefoneResponsavel) {

        super(nome, rg, cpf, dataDeNascimento, endereco, telefone);

        if (valorMensalidade == null)
            throw new RuntimeException("Valor da mensalidade é obrigatório!");

        if (diaVencimentoMensalidade == null)
            throw new RuntimeException("Data de vencimento da mensalidade é obrigatório!");

        if ((LocalDateTime.now().getYear() - dataDeNascimento.getYear()) < 18 && nomeResponsavel == null || telefoneResponsavel == null)
            throw new RuntimeException("Dados do responsável são obrigatório!");

        this.valorMensalidade = valorMensalidade;
        this.diaVencimentoMensalidade = diaVencimentoMensalidade;
        this.nomeResponsavel = nomeResponsavel;
        this.telefoneResponsavel = telefoneResponsavel;
    }
}
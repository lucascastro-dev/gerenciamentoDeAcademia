package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AlunoDto extends PessoaDto {
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private String nomeResponsavel;
    private String telefoneResponsavel;

    public AlunoDto(String nome, String rg, String cpf, LocalDate dataDeNascimento, String endereco, String telefone, Double valorMensalidade, Integer diaVencimentoMensalidade, String nomeResponsavel, String telefoneResponsavel) {
        super(nome, rg, cpf, dataDeNascimento, endereco, telefone);
        this.valorMensalidade = valorMensalidade;
        this.diaVencimentoMensalidade = diaVencimentoMensalidade;
        this.nomeResponsavel = nomeResponsavel;
        this.telefoneResponsavel = telefoneResponsavel;
    }
}
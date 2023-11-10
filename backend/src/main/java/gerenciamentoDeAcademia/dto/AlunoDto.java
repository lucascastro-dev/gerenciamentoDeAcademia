package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlunoDto extends PessoaDto {
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private String nomeResponsavel;
    private String telefoneResponsavel;

    public AlunoDto(Aluno aluno) {
        super(aluno.getNome(),
                aluno.getRg(),
                aluno.getCpf(),
                aluno.getDataDeNascimento(),
                aluno.getEndereco(),
                aluno.getTelefone());
        this.valorMensalidade = aluno.getValorMensalidade();
        this.diaVencimentoMensalidade = aluno.getDiaVencimentoMensalidade();
        this.nomeResponsavel = aluno.getNomeResponsavel();
        this.telefoneResponsavel = aluno.getTelefoneResponsavel();
    }
}
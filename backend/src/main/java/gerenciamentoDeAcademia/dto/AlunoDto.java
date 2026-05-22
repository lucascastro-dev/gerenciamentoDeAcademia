package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlunoDto extends PessoaDto {
    /** Instituição que matricula o aluno (vínculo via turma para login no portal). */
    private Long instituicaoId;
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String faixa;
    private String medida;
}
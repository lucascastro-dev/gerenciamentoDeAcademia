package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TurmaDto {

    private String horario;
    private List<String> dias;
    private String especificacao;
    private FuncionarioDto professor;
    private List<AlunoDto> alunos;

    public TurmaDto(String horario, List<String> dias, String especificacao, FuncionarioDto professor, List<AlunoDto> alunos) {
        this.horario = horario;
        this.dias = dias;
        this.especificacao = especificacao;
        this.professor = professor;
        this.alunos = alunos;
    }
}

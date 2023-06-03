package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
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
    private String modalidade;
    private Funcionario professor;
    private List<Aluno> alunos;

    public TurmaDto(String horario, List<String> dias, String modalidade, Funcionario professor, List<Aluno> alunos) {
        this.horario = horario;
        this.dias = dias;
        this.modalidade = modalidade;
        this.professor = professor;
        this.alunos = alunos;
    }
}

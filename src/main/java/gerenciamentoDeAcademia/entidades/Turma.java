package gerenciamentoDeAcademia.entidades;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Turma {

    private String horario;
    private List<String> dias;
    private String especificacao;
    private Funcionario professor;
    private List<Aluno> alunos;

    public Turma(String horario, List<String> dias, String especificacao, Funcionario professor, List<Aluno> alunos) {
        this.horario = horario;
        this.dias = dias;
        this.especificacao = especificacao;
        this.professor = professor;
        this.alunos = alunos;
    }
}

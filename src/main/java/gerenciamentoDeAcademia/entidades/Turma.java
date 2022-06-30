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
    private String professor;
    private List<String> alunos;

    public Turma(String horario, List<String> dias, String especificacao, String professor, List<String> alunos) {

        if (horario == null)
            throw new RuntimeException("Horário da turma é obrigatório!");

        if (dias == null || dias.size() == 0)
            throw new RuntimeException("Dias de aula são obrigatórios!");

        if (especificacao == null)
            throw new RuntimeException("Especificação da turma é obrigatória!");

        if (professor == null)
            throw new RuntimeException("Professor para a turma é obrigatória!");

        this.horario = horario;
        this.dias = dias;
        this.especificacao = especificacao;
        this.professor = professor;
        this.alunos = alunos;
    }
}

package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TurmaDto {

    private String horario;
    private List<String> dias;
    private String modalidade;
    private String cpfProfessor;
    private List<Aluno> alunos;

    public TurmaDto(Turma turma) {
        this.horario = turma.getHorario();
        this.dias = turma.getDias();
        this.modalidade = turma.getModalidade();
        this.cpfProfessor = turma.getProfessor().getCpf();
        this.alunos = new ArrayList<>();
    }
}

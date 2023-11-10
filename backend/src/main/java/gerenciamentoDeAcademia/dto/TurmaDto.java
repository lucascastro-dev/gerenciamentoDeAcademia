package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Turma;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class TurmaDto {

    private String horario;
    private List<String> dias;
    private String modalidade;
    private String cpfProfessor;
    private List<AlunoDto> alunos;

    public TurmaDto(Turma turma) {
        this.horario = turma.getHorario();
        this.dias = turma.getDias();
        this.modalidade = turma.getModalidade();
        this.cpfProfessor = turma.getProfessor().getCpf();
        this.alunos = turma.getAlunos().stream().map(AlunoDto::new).collect(Collectors.toList());
    }
}

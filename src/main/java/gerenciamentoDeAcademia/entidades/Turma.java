package gerenciamentoDeAcademia.entidades;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_turma")
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String horario;

    @ElementCollection
    @CollectionTable(name = "dias")
    private List<String> dias;

    private String modalidade;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Funcionario professor;

    @ManyToMany
    @JoinTable(name = "turma_aluno",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "aluno_id"))
    private List<Aluno> alunos;

    public Turma(Turma turma) {
        this.id = turma.getId();
        this.horario = turma.getHorario();
        this.dias = turma.getDias();
        this.modalidade = turma.getModalidade();
        this.professor = turma.getProfessor();
        this.alunos = turma.getAlunos();
    }
}
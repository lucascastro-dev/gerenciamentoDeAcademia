package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
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
    @CollectionTable(name = "turma_dias")
    private List<String> dias;

    private String modalidade;

    @OneToOne
    @JoinColumn(name = "professor_id")
    private Funcionario professor;

    @ManyToMany
    @JoinTable(name = "turma_aluno", joinColumns = @JoinColumn(name = "turma_id"), inverseJoinColumns = @JoinColumn(name = "aluno_id"))
    private Set<Aluno> alunos = new HashSet<>();

    public Turma(TurmaDto turmaDto) {
        validar(turmaDto);
        this.horario = turmaDto.getHorario();
        this.dias = turmaDto.getDias();
        this.modalidade = turmaDto.getModalidade();
        this.professor = Funcionario.builder().cpf(turmaDto.getCpfProfessor()).build();
    }

    private void validar(TurmaDto turmaDto) {
        ExcecaoDeDominio.quandoNuloOuVazio(turmaDto.getHorario(), "Horário da turma é obrigatório");
        ExcecaoDeDominio.quandoListaNulaOuVazia(turmaDto.getDias(), "Dias de aula são obrigatórios");
        ExcecaoDeDominio.quandoNuloOuVazio(turmaDto.getModalidade(), "Modalidade da turma é obrigatória");
    }

}
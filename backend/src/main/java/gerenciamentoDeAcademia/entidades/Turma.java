package gerenciamentoDeAcademia.entidades;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_turma")
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String horario;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    private String sala;

    @ElementCollection
    @CollectionTable(name = "turma_dias")
    private List<String> dias;

    private String modalidade;

    @ManyToOne
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @OneToOne
    @JoinColumn(name = "professor_id")
    private Funcionario professor;

    @ManyToMany
    @JoinTable(name = "turma_aluno", joinColumns = @JoinColumn(name = "turma_id"), inverseJoinColumns = @JoinColumn(name = "aluno_id"))
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Aluno> alunos = new HashSet<>();

    /** Hibernate precisa de lista mutável ao substituir {@link ElementCollection}. */
    public void setDias(List<String> dias) {
        this.dias = dias == null ? null : new ArrayList<>(dias);
    }

}
package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.StatusPresenca;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_presenca_aluno", uniqueConstraints = @UniqueConstraint(
        columnNames = {"turma_id", "aluno_cpf", "data_aula"}))
public class PresencaAluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "turma_id", nullable = false)
    private Long turmaId;

    @Column(name = "aluno_cpf", nullable = false, length = 11)
    private String alunoCpf;

    @Column(name = "data_aula", nullable = false)
    private LocalDate dataAula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private StatusPresenca status;

    @Column(name = "atualizado_por_cpf", length = 11)
    private String atualizadoPorCpf;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}

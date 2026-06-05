package gerenciamentoDeAcademia.entidades;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_matricula_instituicao", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"aluno_id", "instituicao_id"})
})
public class MatriculaInstituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Column(nullable = false)
    private Double valorMensalidade;

    @Column(nullable = false)
    private Integer diaVencimentoMensalidade;

    private LocalDate dataUltimoPagamentoMensalidade;
}

package gerenciamentoDeAcademia.entidades;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "tb_conferencia_ponto_mensal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"instituicao_id", "mes_competencia", "ano_competencia"})
)
public class ConferenciaPontoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Column(name = "mes_competencia", nullable = false)
    private Integer mesCompetencia;

    @Column(name = "ano_competencia", nullable = false)
    private Integer anoCompetencia;

    @Column(name = "conferido_em", nullable = false)
    private LocalDateTime conferidoEm;

    @Column(name = "conferido_por_cpf", nullable = false, length = 11)
    private String conferidoPorCpf;

    @Column(name = "integrado_financeiro_em")
    private LocalDateTime integradoFinanceiroEm;

    @Column(name = "integrado_por_cpf", length = 11)
    private String integradoPorCpf;
}

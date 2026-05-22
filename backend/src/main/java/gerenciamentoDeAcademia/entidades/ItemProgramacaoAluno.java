package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_item_programacao_aluno")
public class ItemProgramacaoAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItemProgramacao tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 2000)
    private String descricao;

    private LocalDate dataPrevista;

    /** Texto legado; preferir horaInicio/horaFim quando informados. */
    private String horario;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    private String sala;
}

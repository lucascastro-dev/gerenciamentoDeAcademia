package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.TipoDocumentoRemuneracao;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_documento_remuneracao_colaborador")
public class DocumentoRemuneracaoColaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Column(nullable = false, length = 11)
    private String cpfColaborador;

    @Column(nullable = false)
    private String nomeColaborador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumentoRemuneracao tipo;

    @Column(nullable = false)
    private Integer mesCompetencia;

    @Column(nullable = false)
    private Integer anoCompetencia;

    private BigDecimal valorBruto;

    private BigDecimal valorLiquido;

    @Column(length = 4000)
    private String conteudo;

    private LocalDateTime publicadoEm;

    @Column(length = 11)
    private String publicadoPorCpf;
}

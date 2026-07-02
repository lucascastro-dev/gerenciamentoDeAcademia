package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.StatusCobrancaExterna;
import gerenciamentoDeAcademia.enums.TipoCobrancaExterna;
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
@Table(name = "tb_cobranca_externa")
public class CobrancaExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Column(length = 11)
    private String cpfAluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCobrancaExterna tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCobrancaExterna status;

    private BigDecimal valor;

    private Integer mesCompetencia;

    private Integer anoCompetencia;

    @Column(length = 80)
    private String idExterno;

    @Column(length = 500)
    private String urlPagamento;

    @Column(length = 40)
    private String provedor;

    /** Código do plano (PLANO_INSTITUICAO) ou referência auxiliar */
    @Column(length = 40)
    private String referencia;

    @Column(name = "billing_type", length = 20)
    private String billingType;

    @Column(name = "pix_qr_code", length = 8000)
    private String pixQrCode;

    @Column(name = "pix_copia_cola", length = 800)
    private String pixCopiaCola;

    private LocalDateTime criadoEm;

    private LocalDateTime pagoEm;
}

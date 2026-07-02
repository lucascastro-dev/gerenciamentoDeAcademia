package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.StatusSolicitacaoFerias;
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
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_solicitacao_ferias")
public class SolicitacaoFerias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Column(name = "cpf_colaborador", nullable = false, length = 11)
    private String cpfColaborador;

    @Column(name = "nome_colaborador", nullable = false)
    private String nomeColaborador;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "dias_solicitados", nullable = false)
    private Integer diasSolicitados;

    @Column(name = "inicio_periodo_aquisitivo", nullable = false)
    private LocalDate inicioPeriodoAquisitivo;

    @Column(name = "fim_periodo_aquisitivo", nullable = false)
    private LocalDate fimPeriodoAquisitivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacaoFerias status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    @Column(name = "decidido_por_cpf", length = 11)
    private String decididoPorCpf;

    @Column(name = "observacao_rh", length = 500)
    private String observacaoRh;
}

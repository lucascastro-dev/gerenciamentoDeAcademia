package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.StatusSolicitacaoAjustePonto;
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
@Table(name = "tb_solicitacao_ajuste_ponto")
public class SolicitacaoAjustePonto {

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

    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro;

    @Column(name = "hora_entrada_atual")
    private LocalDateTime horaEntradaAtual;

    @Column(name = "hora_saida_atual")
    private LocalDateTime horaSaidaAtual;

    @Column(name = "hora_entrada_proposta")
    private LocalDateTime horaEntradaProposta;

    @Column(name = "hora_saida_proposta")
    private LocalDateTime horaSaidaProposta;

    @Column(nullable = false, length = 500)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacaoAjustePonto status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    @Column(name = "decidido_por_cpf", length = 11)
    private String decididoPorCpf;

    @Column(name = "observacao_gestor", length = 500)
    private String observacaoGestor;
}

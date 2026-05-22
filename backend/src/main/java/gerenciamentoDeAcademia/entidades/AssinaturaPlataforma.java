package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_assinatura_plataforma")
public class AssinaturaPlataforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "instituicao_id", nullable = false, unique = true)
    private Instituicao instituicao;

    @Enumerated(EnumType.STRING)
    private PlanoInstituicaoTipo plano;

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean ativo;

    public boolean isVigente() {
        if (!Boolean.TRUE.equals(ativo)) {
            return false;
        }
        LocalDate hoje = LocalDate.now();
        return dataInicio != null
                && !hoje.isBefore(dataInicio)
                && dataFim != null
                && !hoje.isAfter(dataFim);
    }
}

package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AssinaturaPlataformaDto {
    private Long instituicaoId;
    private PlanoInstituicaoTipo plano;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private boolean ativo;
    private boolean vigente;

    public static AssinaturaPlataformaDto of(AssinaturaPlataforma assinatura) {
        AssinaturaPlataformaDto dto = new AssinaturaPlataformaDto();
        if (assinatura == null) {
            dto.setAtivo(false);
            dto.setVigente(false);
            return dto;
        }
        dto.setInstituicaoId(assinatura.getInstituicao().getId());
        dto.setPlano(assinatura.getPlano());
        dto.setDataInicio(assinatura.getDataInicio());
        dto.setDataFim(assinatura.getDataFim());
        dto.setAtivo(Boolean.TRUE.equals(assinatura.getAtivo()));
        dto.setVigente(assinatura.isVigente());
        return dto;
    }
}

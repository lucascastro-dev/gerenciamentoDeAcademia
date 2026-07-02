package gerenciamentoDeAcademia.dto.integracoes;

import gerenciamentoDeAcademia.entidades.CobrancaExterna;
import gerenciamentoDeAcademia.enums.StatusCobrancaExterna;
import gerenciamentoDeAcademia.enums.TipoCobrancaExterna;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CobrancaExternaDto(
        Long id,
        TipoCobrancaExterna tipo,
        StatusCobrancaExterna status,
        BigDecimal valor,
        Integer mesCompetencia,
        Integer anoCompetencia,
        String urlPagamento,
        String idExterno,
        String billingType,
        String pixQrCode,
        String pixCopiaCola,
        boolean modoLocal,
        LocalDateTime criadoEm,
        LocalDateTime pagoEm
) {
    public static CobrancaExternaDto of(CobrancaExterna cobranca, boolean modoLocal) {
        if (cobranca == null) {
            return null;
        }
        return new CobrancaExternaDto(
                cobranca.getId(),
                cobranca.getTipo(),
                cobranca.getStatus(),
                cobranca.getValor(),
                cobranca.getMesCompetencia(),
                cobranca.getAnoCompetencia(),
                cobranca.getUrlPagamento(),
                cobranca.getIdExterno(),
                cobranca.getBillingType(),
                cobranca.getPixQrCode(),
                cobranca.getPixCopiaCola(),
                modoLocal,
                cobranca.getCriadoEm(),
                cobranca.getPagoEm());
    }
}

package gerenciamentoDeAcademia.dto.integracoes;

import java.util.Map;

public record IntegracoesStatusDto(
        boolean modoLocal,
        boolean brevoAtivo,
        boolean twilioAtivo,
        boolean asaasAtivo
) {
    public Map<String, Object> asMap() {
        return Map.of(
                "modoLocal", modoLocal,
                "brevo", brevoAtivo,
                "twilio", twilioAtivo,
                "asaas", asaasAtivo);
    }
}

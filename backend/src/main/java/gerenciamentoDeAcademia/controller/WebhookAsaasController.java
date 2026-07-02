package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.servicos.integracoes.ServicoCobrancaExterna;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("webhooks")
@RequiredArgsConstructor
public class WebhookAsaasController {

    private final ServicoCobrancaExterna servicoCobrancaExterna;

    @Value("${app.integracoes.asaas.webhook-token:}")
    private String webhookToken;

    @PostMapping("/asaas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void asaas(
            @RequestHeader(value = "asaas-access-token", required = false) String token,
            @RequestBody Map<String, Object> payload) {
        if (webhookToken != null && !webhookToken.isBlank()
                && (token == null || !webhookToken.equals(token))) {
            log.warn("Webhook Asaas rejeitado: token inválido");
            return;
        }
        servicoCobrancaExterna.processarWebhookAsaas(payload);
    }
}

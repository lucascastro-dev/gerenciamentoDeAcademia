package gerenciamentoDeAcademia.infra.integracoes;

import gerenciamentoDeAcademia.dto.integracoes.CriarCobrancaMensalidadeFormDto;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ProvedorPagamentoAsaas implements ProvedorPagamento {

    private static final DateTimeFormatter ASAAS_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate;

    @Value("${app.integracoes.asaas.enabled:false}")
    private boolean asaasEnabled;

    @Value("${app.integracoes.modo-local:true}")
    private boolean modoLocal;

    @Value("${app.integracoes.asaas.api-key:}")
    private String apiKey;

    @Value("${app.integracoes.asaas.base-url:https://sandbox.asaas.com/api/v3}")
    private String baseUrl;

    public ProvedorPagamentoAsaas(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Override
    public boolean ativo() {
        return asaasEnabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public RespostaCobrancaProvedor criarCobranca(SolicitacaoCobrancaExterna solicitacao) {
        if (modoLocal || !ativo()) {
            return cobrancaLocal(solicitacao);
        }
        return criarCobrancaAsaas(solicitacao);
    }

    private RespostaCobrancaProvedor cobrancaLocal(SolicitacaoCobrancaExterna solicitacao) {
        String idLocal = "local-" + UUID.randomUUID();
        String billing = solicitacao.billingType() != null ? solicitacao.billingType() : "UNDEFINED";
        log.info("COBRANCA_LOCAL tipo={} billing={} valor={} id={}",
                solicitacao.tipo(), billing, solicitacao.valor(), idLocal);
        if ("PIX".equalsIgnoreCase(billing)) {
            return new RespostaCobrancaProvedor(
                    idLocal,
                    "/arealogada/aluno/mensalidades?cobrancaSimulada=" + idLocal,
                    modoLocal ? "local" : "asaas-sandbox",
                    "PIX",
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
                    "00020126580014br.gov.bcb.pix0136" + idLocal + "5204000053039865802BR5925Turma3606009SAO PAULO62070503***6304ABCD");
        }
        return new RespostaCobrancaProvedor(
                idLocal,
                "/arealogada/aluno/mensalidades?cobrancaSimulada=" + idLocal,
                modoLocal ? "local" : "asaas-sandbox",
                billing,
                null,
                null);
    }

    @SuppressWarnings("unchecked")
    private RespostaCobrancaProvedor criarCobrancaAsaas(SolicitacaoCobrancaExterna solicitacao) {
        String customerId = garantirCliente(solicitacao);
        String billingType = normalizarBillingType(solicitacao.billingType());
        LocalDate dueDate = solicitacao.dueDate() != null ? solicitacao.dueDate() : LocalDate.now().plusDays(3);

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("billingType", billingType);
        body.put("value", solicitacao.valor());
        body.put("dueDate", dueDate.format(ASAAS_DATE));
        body.put("description", solicitacao.descricao());

        if ("CREDIT_CARD".equals(billingType)) {
            CriarCobrancaMensalidadeFormDto.DadosCartaoCreditoFormDto cartao = solicitacao.cartao();
            ExcecaoDeDominio.quandoNulo(cartao, "Dados do cartão são obrigatórios.");
            body.put("creditCard", Map.of(
                    "holderName", cartao.holderName(),
                    "number", cartao.number().replaceAll("\\D", ""),
                    "expiryMonth", cartao.expiryMonth(),
                    "expiryYear", cartao.expiryYear(),
                    "ccv", cartao.ccv()));
            body.put("creditCardHolderInfo", Map.of(
                    "name", cartao.holderName(),
                    "email", cartao.holderEmail(),
                    "cpfCnpj", cartao.holderCpf().replaceAll("\\D", ""),
                    "postalCode", cartao.holderPostalCode().replaceAll("\\D", ""),
                    "addressNumber", cartao.holderAddressNumber(),
                    "phone", cartao.holderPhone().replaceAll("\\D", "")));
        }

        Map<String, Object> resposta = postJson("/lean/payments", body);
        String id = String.valueOf(resposta.get("id"));
        String url = resposta.get("invoiceUrl") != null
                ? String.valueOf(resposta.get("invoiceUrl"))
                : String.valueOf(resposta.getOrDefault("bankSlipUrl", ""));

        String pixQr = null;
        String pixCopia = null;
        if ("PIX".equals(billingType)) {
            Map<String, Object> pix = getJson("/payments/" + id + "/pixQrCode");
            if (pix != null) {
                pixQr = pix.get("encodedImage") != null ? String.valueOf(pix.get("encodedImage")) : null;
                pixCopia = pix.get("payload") != null ? String.valueOf(pix.get("payload")) : null;
            }
        }

        return new RespostaCobrancaProvedor(id, url, "asaas", billingType, pixQr, pixCopia);
    }

    @SuppressWarnings("unchecked")
    private String garantirCliente(SolicitacaoCobrancaExterna solicitacao) {
        String cpf = solicitacao.cpfPagador().replaceAll("\\D", "");
        Map<String, Object> busca = getJson("/customers?cpfCnpj=" + cpf);
        if (busca != null && busca.get("data") instanceof List<?> lista && !lista.isEmpty()) {
            Object primeiro = lista.get(0);
            if (primeiro instanceof Map<?, ?> mapa) {
                return String.valueOf(mapa.get("id"));
            }
        }

        Map<String, Object> novo = new HashMap<>();
        novo.put("name", solicitacao.nomePagador());
        novo.put("cpfCnpj", cpf);
        if (solicitacao.emailPagador() != null && !solicitacao.emailPagador().isBlank()) {
            novo.put("email", solicitacao.emailPagador());
        }
        novo.put("notificationDisabled", false);

        Map<String, Object> criado = postJson("/customers", novo);
        return String.valueOf(criado.get("id"));
    }

    private String normalizarBillingType(String billingType) {
        if (billingType == null || billingType.isBlank()) {
            return "UNDEFINED";
        }
        return switch (billingType.toUpperCase()) {
            case "PIX" -> "PIX";
            case "CREDIT_CARD", "CARTAO", "CARTAO_CREDITO" -> "CREDIT_CARD";
            default -> "UNDEFINED";
        };
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postJson(String path, Map<String, Object> body) {
        try {
            Map<String, Object> resposta = restTemplate.postForObject(
                    baseUrl + path,
                    new HttpEntity<>(body, headers()),
                    Map.class);
            if (resposta == null) {
                throw new ExcecaoDeDominio("Resposta vazia do Asaas.");
            }
            return resposta;
        } catch (ExcecaoDeDominio e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro Asaas POST {}: {}", path, e.getMessage());
            throw new ExcecaoDeDominio("Não foi possível gerar a cobrança no Asaas.");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getJson(String path) {
        try {
            return restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.GET,
                    new HttpEntity<>(headers()),
                    Map.class).getBody();
        } catch (Exception e) {
            log.warn("Erro Asaas GET {}: {}", path, e.getMessage());
            return null;
        }
    }
}

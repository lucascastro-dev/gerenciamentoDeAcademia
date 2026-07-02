package gerenciamentoDeAcademia.infra.integracoes;

public record RespostaCobrancaProvedor(
        String idExterno,
        String urlPagamento,
        String provedor,
        String billingType,
        String pixQrCode,
        String pixCopiaCola
) {
    public RespostaCobrancaProvedor(String idExterno, String urlPagamento, String provedor) {
        this(idExterno, urlPagamento, provedor, null, null, null);
    }
}

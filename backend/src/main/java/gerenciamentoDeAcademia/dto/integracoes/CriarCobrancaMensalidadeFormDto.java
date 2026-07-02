package gerenciamentoDeAcademia.dto.integracoes;

public record CriarCobrancaMensalidadeFormDto(
        String formaPagamento,
        DadosCartaoCreditoFormDto cartao
) {
    public record DadosCartaoCreditoFormDto(
            String holderName,
            String number,
            String expiryMonth,
            String expiryYear,
            String ccv,
            String holderEmail,
            String holderCpf,
            String holderPostalCode,
            String holderAddressNumber,
            String holderPhone
    ) {}
}

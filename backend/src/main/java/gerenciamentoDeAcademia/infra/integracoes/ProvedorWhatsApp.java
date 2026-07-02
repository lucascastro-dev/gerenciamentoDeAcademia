package gerenciamentoDeAcademia.infra.integracoes;

public interface ProvedorWhatsApp {
    boolean ativo();

    void enviar(MensagemWhatsApp mensagem);
}

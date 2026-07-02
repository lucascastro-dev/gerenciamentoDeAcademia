package gerenciamentoDeAcademia.infra.integracoes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProvedorWhatsAppLog implements ProvedorWhatsApp {

    @Value("${app.integracoes.twilio.enabled:false}")
    private boolean twilioEnabled;

    @Override
    public boolean ativo() {
        return twilioEnabled;
    }

    @Override
    public void enviar(MensagemWhatsApp mensagem) {
        log.info("NOTIFICACAO_WHATSAPP telefone={} corpo={}",
                mensagem.getTelefoneE164(),
                mensagem.getCorpo());
    }
}

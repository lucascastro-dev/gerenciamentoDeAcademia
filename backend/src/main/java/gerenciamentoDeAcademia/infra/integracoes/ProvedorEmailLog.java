package gerenciamentoDeAcademia.infra.integracoes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProvedorEmailLog implements ProvedorEmail {

    @Value("${app.integracoes.brevo.enabled:false}")
    private boolean brevoEnabled;

    @Override
    public boolean ativo() {
        return brevoEnabled;
    }

    @Override
    public void enviar(MensagemEmail mensagem) {
        log.info("NOTIFICACAO_EMAIL destinatario={} assunto={} corpo={}",
                mensagem.getDestinatario(),
                mensagem.getAssunto(),
                mensagem.getCorpoTexto());
    }
}

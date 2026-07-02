package gerenciamentoDeAcademia.infra.integracoes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MensagemEmail {
    private final String destinatario;
    private final String assunto;
    private final String corpoTexto;
}

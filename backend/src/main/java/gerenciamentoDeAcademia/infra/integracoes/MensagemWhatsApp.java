package gerenciamentoDeAcademia.infra.integracoes;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MensagemWhatsApp {
    private final String telefoneE164;
    private final String corpo;
}

package gerenciamentoDeAcademia.servicos.integracoes;

import gerenciamentoDeAcademia.dto.integracoes.IntegracoesStatusDto;
import gerenciamentoDeAcademia.infra.integracoes.MensagemEmail;
import gerenciamentoDeAcademia.infra.integracoes.MensagemWhatsApp;
import gerenciamentoDeAcademia.infra.integracoes.ProvedorEmail;
import gerenciamentoDeAcademia.infra.integracoes.ProvedorWhatsApp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicoNotificacoes {

    private final ProvedorEmail provedorEmail;
    private final ProvedorWhatsApp provedorWhatsApp;

    @Value("${app.integracoes.modo-local:true}")
    private boolean modoLocal;

    @Value("${app.integracoes.brevo.enabled:false}")
    private boolean brevoEnabled;

    @Value("${app.integracoes.twilio.enabled:false}")
    private boolean twilioEnabled;

    @Value("${app.integracoes.asaas.enabled:false}")
    private boolean asaasEnabled;

    public void enviarEmail(MensagemEmail mensagem) {
        provedorEmail.enviar(mensagem);
    }

    public void enviarWhatsApp(MensagemWhatsApp mensagem) {
        provedorWhatsApp.enviar(mensagem);
    }

    public void enviarRecuperacaoSenha(String email, String cpfMascarado) {
        if (email == null || email.isBlank()) {
            return;
        }
        enviarEmail(MensagemEmail.builder()
                .destinatario(email)
                .assunto("Turma360 — recuperação de senha")
                .corpoTexto("""
                        Recebemos seu pedido de recuperação de senha (CPF %s).

                        Em produção, este e-mail conterá o link seguro de redefinição (Brevo).
                        Em modo local, confira o log do servidor NOTIFICACAO_EMAIL.
                        """.formatted(cpfMascarado))
                .build());
    }

    public void avisarCobrancaWhatsApp(String telefone, String mensagem) {
        if (telefone == null || telefone.isBlank()) {
            return;
        }
        enviarWhatsApp(MensagemWhatsApp.builder()
                .telefoneE164(normalizarTelefone(telefone))
                .corpo(mensagem)
                .build());
    }

    public void avisarCobrancaEmail(String email, String mensagem, String urlPagamento) {
        if (email == null || email.isBlank()) {
            return;
        }
        String corpo = mensagem;
        if (urlPagamento != null && !urlPagamento.isBlank()) {
            corpo += "\n\nLink de pagamento: " + urlPagamento;
        }
        enviarEmail(MensagemEmail.builder()
                .destinatario(email)
                .assunto("Turma360 — cobrança de mensalidade")
                .corpoTexto(corpo)
                .build());
    }

    public IntegracoesStatusDto status() {
        return new IntegracoesStatusDto(
                modoLocal,
                brevoEnabled && provedorEmail.ativo(),
                twilioEnabled && provedorWhatsApp.ativo(),
                asaasEnabled);
    }

    private String normalizarTelefone(String telefone) {
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.startsWith("55")) {
            return "+" + digitos;
        }
        return "+55" + digitos;
    }
}

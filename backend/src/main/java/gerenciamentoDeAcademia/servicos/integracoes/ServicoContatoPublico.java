package gerenciamentoDeAcademia.servicos.integracoes;

import gerenciamentoDeAcademia.dto.ContatoPublicoRequest;
import gerenciamentoDeAcademia.infra.integracoes.MensagemEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicoContatoPublico {

    private final ServicoNotificacoes servicoNotificacoes;

    @Value("${app.contato.email:comercial@turma360.com.br}")
    private String emailComercial;

    public void enviar(ContatoPublicoRequest request) {
        String corpo = """
                Novo contato pelo site Turma360

                Nome: %s
                E-mail: %s
                Telefone: %s
                Instituição: %s

                Mensagem:
                %s
                """.formatted(
                request.getNome(),
                request.getEmail(),
                valorOuNaoInformado(request.getTelefone()),
                valorOuNaoInformado(request.getInstituicao()),
                request.getMensagem());

        servicoNotificacoes.enviarEmail(MensagemEmail.builder()
                .destinatario(emailComercial)
                .assunto("Turma360 — contato: " + request.getNome())
                .corpoTexto(corpo)
                .build());

        servicoNotificacoes.enviarEmail(MensagemEmail.builder()
                .destinatario(request.getEmail())
                .assunto("Turma360 — recebemos sua mensagem")
                .corpoTexto("""
                        Olá, %s!

                        Recebemos sua mensagem e nossa equipe comercial retornará em até 1 dia útil.
                        Horário de atendimento: segunda a sexta, das 9h às 18h.

                        Turma360 — gestão educacional inteligente
                        """.formatted(request.getNome()))
                .build());
    }

    private String valorOuNaoInformado(String valor) {
        return valor == null || valor.isBlank() ? "(não informado)" : valor.trim();
    }
}

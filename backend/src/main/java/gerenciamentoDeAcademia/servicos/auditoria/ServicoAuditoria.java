package gerenciamentoDeAcademia.servicos.auditoria;

import gerenciamentoDeAcademia.entidades.RegistroAuditoria;
import gerenciamentoDeAcademia.repositorios.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicoAuditoria {

    private static final Logger log = LoggerFactory.getLogger(ServicoAuditoria.class);
    private final AuditoriaRepository auditoriaRepository;

    public void registrar(String acao, String entidade, String identificador, String detalhes) {
        String login = obterLoginAtual();
        String ip = obterIpCliente();

        RegistroAuditoria registro = RegistroAuditoria.builder()
                .dataHora(LocalDateTime.now())
                .usuarioLogin(login != null ? login : "sistema")
                .acao(acao)
                .entidade(entidade)
                .identificador(identificador)
                .detalhes(detalhes)
                .enderecoIp(ip)
                .build();

        auditoriaRepository.save(registro);
        log.info("AUDITORIA [{}] {} em {} ({}) - {}", login, acao, entidade, identificador, detalhes);
    }

    private String obterLoginAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName();
    }

    private String obterIpCliente() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                return attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {
            // contexto fora de requisição HTTP
        }
        return null;
    }
}

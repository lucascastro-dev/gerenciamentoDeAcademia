package gerenciamentoDeAcademia.infra.seguranca;

import com.fasterxml.jackson.databind.ObjectMapper;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.servicos.cobranca.ServicoSituacaoCobranca;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Bloqueia operações de colaboradores quando o plano da instituição (vínculo do JWT) não está ativo.
 */
@Component
public class FiltroPlanoInstituicao extends OncePerRequestFilter {

    public static final String CODIGO_PLANO_INATIVO = "PLANO_INSTITUICAO_INATIVO";

    private static final List<String> PREFIXOS_LIBERADOS = List.of(
            "/login",
            "/plano-instituicao",
            "/academia/consultarAcademiaId/",
            "/instituicao/consultarAcademiaId/",
            "/funcionario/meuPerfil",
            "/portal-aluno",
            "/programacao"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (caminhoLiberado(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            if (!usuario.isPortalAluno()
                    && !isMaster(usuario)
                    && usuario.getSituacaoCobranca() == SituacaoCobranca.BLOQUEADO) {
                responderPlanoInativo(response, ServicoSituacaoCobranca.MSG_BLOQUEIO_INSTITUICAO);
                return;
            }
            if (usuario.isPortalAluno()
                    && usuario.getSituacaoCobranca() == SituacaoCobranca.BLOQUEADO) {
                responderPlanoInativo(response, ServicoSituacaoCobranca.MSG_BLOQUEIO_ALUNO);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMaster(UsuarioAutenticado usuario) {
        return usuario.getFuncionario() != null && usuario.getFuncionario().isUsuarioMaster();
    }

    private boolean caminhoLiberado(HttpServletRequest request) {
        String uri = normalizarUri(request);
        return PREFIXOS_LIBERADOS.stream().anyMatch(uri::contains);
    }

    private String normalizarUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        return uri;
    }

    private void responderPlanoInativo(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "code", CODIGO_PLANO_INATIVO,
                "message", mensagem
        ));
    }
}

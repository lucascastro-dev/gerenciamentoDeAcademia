package gerenciamentoDeAcademia.infra.seguranca;

import com.fasterxml.jackson.databind.ObjectMapper;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
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
 * Bloqueia operações quando o plano da instituição expirou ou o pagamento ainda não foi confirmado.
 */
@Component
public class FiltroPlanoInstituicao extends OncePerRequestFilter {

    public static final String CODIGO_PLANO_INATIVO = "PLANO_INSTITUICAO_INATIVO";
    public static final String CODIGO_PAGAMENTO_PENDENTE = "PAGAMENTO_PENDENTE";

    private static final List<String> PREFIXOS_LIBERADOS_PLANO = List.of(
            "/login",
            "/plano-instituicao",
            "/academia/consultarAcademiaId/",
            "/instituicao/consultarAcademiaId/",
            "/funcionario/meuPerfil",
            "/portal-aluno"
    );

    private static final List<String> PREFIXOS_LIBERADOS_PAGAMENTO_PENDENTE = List.of(
            "/login",
            "/plano-instituicao",
            "/funcionario/meuPerfil",
            "/funcionario/alterarSenha"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (caminhoLiberado(request, PREFIXOS_LIBERADOS_PLANO)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            if (usuario.isOperadorPlataforma()) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!usuario.isPortalAluno()
                    && usuario.getSituacaoCobranca() == SituacaoCobranca.BLOQUEADO) {
                responder(response, CODIGO_PLANO_INATIVO, ServicoSituacaoCobranca.MSG_BLOQUEIO_INSTITUICAO);
                return;
            }
            if (usuario.isPortalAluno()
                    && usuario.getSituacaoCobranca() == SituacaoCobranca.BLOQUEADO) {
                responder(response, CODIGO_PLANO_INATIVO, ServicoSituacaoCobranca.MSG_BLOQUEIO_ALUNO);
                return;
            }
            if (!usuario.isPortalAluno()
                    && usuario.getStatusFinanceiroInstituicao() == StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO) {
                if (!caminhoLiberado(request, PREFIXOS_LIBERADOS_PAGAMENTO_PENDENTE)) {
                    boolean admin = usuario.getFuncionario() != null
                            && usuario.getFuncionario().getTipoFuncionario() == TipoFuncionario.ADMINISTRADOR;
                    if (!admin) {
                        responder(response, CODIGO_PAGAMENTO_PENDENTE,
                                "Pagamento da instituição pendente. Aguarde a confirmação pelo master da plataforma.");
                        return;
                    }
                    responder(response, CODIGO_PAGAMENTO_PENDENTE,
                            "Pagamento pendente. Apenas cadastro, plano e senha estão disponíveis até a confirmação.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean caminhoLiberado(HttpServletRequest request, List<String> prefixos) {
        String uri = normalizarUri(request);
        return prefixos.stream().anyMatch(uri::contains);
    }

    private String normalizarUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        return uri;
    }

    private void responder(HttpServletResponse response, String code, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "code", code,
                "message", mensagem
        ));
    }
}

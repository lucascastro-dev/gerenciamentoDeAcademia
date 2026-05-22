package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FiltroSeguranca extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroSeguranca.class);

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            var token = recoverToken(request);

            if (token != null) {
                ClaimsSessao claims = tokenService.extrairClaims(token);
                var login = claims != null ? claims.getLogin() : "";

                if (login != null && !login.isBlank()) {
                    var usuario = usuarioRepository.findByLogin(login);

                    if (usuario != null) {
                        UsuarioAutenticado autenticado;
                        Long instituicaoId = claims != null ? claims.getInstituicaoId() : null;
                        SituacaoCobranca situacao = claims != null ? claims.getSituacaoCobranca() : SituacaoCobranca.ATIVO;
                        if (usuario.getRole() == UserRole.ALUNO) {
                            autenticado = new UsuarioAutenticado(
                                    usuario, null, alunoRepository.findByCpf(login), instituicaoId, situacao);
                        } else {
                            autenticado = new UsuarioAutenticado(
                                    usuario, funcionarioRepository.findByCpf(login), null, instituicaoId, situacao);
                        }
                        var autenticacao = new UsernamePasswordAuthenticationToken(
                                autenticado, null, autenticado.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(autenticacao);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao processar token JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}

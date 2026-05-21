package gerenciamentoDeAcademia.infra.seguranca;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(UsuarioAutenticado usuarioAutenticado) {
        try {
            TipoFuncionario tipo = usuarioAutenticado.getFuncionario() != null
                    ? usuarioAutenticado.getFuncionario().getTipoFuncionario()
                    : null;
            List<String> permissoes;
            if (usuarioAutenticado.isPortalAluno()) {
                permissoes = List.of(
                        "aluno-portal:dados",
                        "aluno-portal:turmas",
                        "aluno-portal:mensalidades",
                        "aluno-portal:pagamento"
                );
            } else if (tipo != null) {
                permissoes = TipoFuncionario.codigosPermissao(
                        tipo,
                        usuarioAutenticado.getFuncionario().getAreaTerceirizado()).stream().toList();
            } else {
                permissoes = List.of();
            }

            Algorithm algorithm = Algorithm.HMAC256(secret);
            var builder = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(usuarioAutenticado.getUsername())
                    .withClaim("master", usuarioAutenticado.getFuncionario() != null
                            && usuarioAutenticado.getFuncionario().isUsuarioMaster())
                    .withClaim("portalAluno", usuarioAutenticado.isPortalAluno())
                    .withExpiresAt(gerarDataValidade());

            if (tipo != null) {
                builder.withClaim("tipo", tipo.name());
            }
            if (!permissoes.isEmpty()) {
                builder.withArrayClaim("permissoes", permissoes.toArray(new String[0]));
            }
            return builder.sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    public String validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    private Instant gerarDataValidade() {
        return LocalDateTime.now().plusHours(8).toInstant(ZoneOffset.of("-03:00"));
    }
}

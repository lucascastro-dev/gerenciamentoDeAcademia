package gerenciamentoDeAcademia.infra.seguranca;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
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

    public String gerarToken(UsuarioAutenticado usuarioAutenticado, String vinculoInstituicao, SituacaoCobranca situacao) {
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
                        "aluno-portal:pagamento",
                        "aluno-portal:senha",
                        "aluno-portal:programacao"
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
            if (vinculoInstituicao != null && !vinculoInstituicao.isBlank()) {
                builder.withClaim("vinculo", vinculoInstituicao);
            }
            SituacaoCobranca sit = situacao != null ? situacao : SituacaoCobranca.ATIVO;
            builder.withClaim("situacaoCobranca", sit.name());
            builder.withClaim("planoAtivo", sit.permiteAcesso());
            return builder.sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    public String validarToken(String token) {
        ClaimsSessao claims = extrairClaims(token);
        return claims != null ? claims.getLogin() : "";
    }

    public ClaimsSessao extrairClaims(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
            Long instituicaoId = null;
            String vinculo = jwt.getClaim("vinculo").asString();
            if (vinculo != null && !vinculo.isBlank()) {
                try {
                    instituicaoId = Long.parseLong(vinculo);
                } catch (NumberFormatException ignored) {
                    // mantém null
                }
            }
            SituacaoCobranca situacao = resolverSituacaoClaim(jwt);
            boolean master = Boolean.TRUE.equals(jwt.getClaim("master").asBoolean());
            boolean portalAluno = Boolean.TRUE.equals(jwt.getClaim("portalAluno").asBoolean());
            return new ClaimsSessao(jwt.getSubject(), instituicaoId, situacao, master, portalAluno);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    private SituacaoCobranca resolverSituacaoClaim(DecodedJWT jwt) {
        String situacaoStr = jwt.getClaim("situacaoCobranca").asString();
        if (situacaoStr != null && !situacaoStr.isBlank()) {
            try {
                return SituacaoCobranca.valueOf(situacaoStr);
            } catch (IllegalArgumentException ignored) {
                // compatibilidade com tokens antigos
            }
        }
        Boolean planoClaim = jwt.getClaim("planoAtivo").asBoolean();
        return Boolean.FALSE.equals(planoClaim) ? SituacaoCobranca.BLOQUEADO : SituacaoCobranca.ATIVO;
    }

    private Instant gerarDataValidade() {
        return LocalDateTime.now().plusHours(8).toInstant(ZoneOffset.of("-03:00"));
    }
}

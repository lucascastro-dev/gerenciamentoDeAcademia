package gerenciamentoDeAcademia.infra.seguranca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

class PermissaoEvaluatorTest {

    private final PermissaoEvaluator evaluator = new PermissaoEvaluator();

    @Test
    @DisplayName("Dado usuário com uma das permissões, Quando possuiAlguma, Então autoriza")
    void deveAutorizarQuandoPossuiUmaDasPermissoes() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user",
                "pwd",
                List.of(new SimpleGrantedAuthority("programacao:gerenciar-itens")));

        Assertions.assertTrue(evaluator.possuiAlguma(auth, "programacao:gerenciar", "programacao:gerenciar-itens"));
    }

    @Test
    @DisplayName("Dado usuário sem permissões, Quando possuiAlguma, Então nega")
    void deveNegarQuandoNaoPossuiNenhumaPermissao() {
        var auth = new UsernamePasswordAuthenticationToken("user", "pwd", List.of());

        Assertions.assertFalse(evaluator.possuiAlguma(auth, "programacao:gerenciar", "programacao:gerenciar-itens"));
    }

    @Test
    @DisplayName("Dado master, Quando possui qualquer permissão, Então autoriza")
    void deveAutorizarMasterParaQualquerPermissao() {
        var auth = new UsernamePasswordAuthenticationToken(
                "master",
                "pwd",
                List.of(new SimpleGrantedAuthority("ROLE_MASTER")));

        Assertions.assertTrue(evaluator.possui(auth, "turma:gerenciar-alunos"));
    }
}

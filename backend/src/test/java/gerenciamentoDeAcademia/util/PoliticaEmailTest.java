package gerenciamentoDeAcademia.util;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PoliticaEmailTest {

    @Test
    void deveAceitarEmailValido() {
        Assertions.assertTrue(PoliticaEmail.ehValido("colaborador@castro.edu.br"));
        Assertions.assertDoesNotThrow(() -> PoliticaEmail.validar("colaborador@castro.edu.br"));
        Assertions.assertEquals("colaborador@castro.edu.br", PoliticaEmail.normalizar("  colaborador@castro.edu.br  "));
    }

    @Test
    void deveRejeitarEmailInvalido() {
        Assertions.assertFalse(PoliticaEmail.ehValido("email-invalido"));
        Assertions.assertFalse(PoliticaEmail.ehValido("sem-arroba.com"));
        Assertions.assertFalse(PoliticaEmail.ehValido("@dominio.com"));

        var erro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> PoliticaEmail.validar("email-invalido"));
        Assertions.assertTrue(erro.getMessage().contains("formato inválido"));
    }

    @Test
    void deveRejeitarEmailVazio() {
        var erro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> PoliticaEmail.validar("   "));
        Assertions.assertEquals("E-mail é obrigatório", erro.getMessage());
    }
}

package gerenciamentoDeAcademia.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MascaramentoDadosUtilTest {

    @Test
    @DisplayName("Dado CPF completo, Quando mascarar, Então oculta início e fim")
    void deveMascararCpf() {
        Assertions.assertEquals("***.825.820-**", MascaramentoDadosUtil.cpf("61482582007"));
    }

    @Test
    @DisplayName("Dado telefone com DDD, Quando mascarar, Então mantém DDD e final")
    void deveMascararTelefone() {
        Assertions.assertEquals("(21) *****-1234", MascaramentoDadosUtil.telefone("21999991234"));
    }

    @Test
    @DisplayName("Dado e-mail, Quando mascarar, Então oculta parte local")
    void deveMascararEmail() {
        Assertions.assertEquals("l***@email.com", MascaramentoDadosUtil.email("lucas@email.com"));
    }
}

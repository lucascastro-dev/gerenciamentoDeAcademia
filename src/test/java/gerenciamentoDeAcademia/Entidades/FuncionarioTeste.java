package gerenciamentoDeAcademia.Entidades;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class FuncionarioTeste {

    @Test
    public void cargo_nao_pode_ser_nulo() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDateTime.now(), "Rua logo ali", "2111111-11111", null, "Programação");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Cargo é obrigatório!");
        }
    }

    @Test
    public void especializacao_eh_obrigatoria_se_for_professor() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDateTime.now(), "Rua logo ali", "2111111-11111", "Professor", null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Especialização é obrigatória para professor!");
        }
    }

    @Test
    public void nao_deve_estourar_excecao_se_cargo_for_diferente_de_professor() {
        Assertions.assertDoesNotThrow(() -> new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                LocalDateTime.now(), "Rua logo ali", "2111111-11111", "Estagiário", null));
    }
}
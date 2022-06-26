package gerenciamentoDeAcademia;

import gerenciamentoDeAcademia.Entidades.Aluno;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class AlunoTeste {

    @Test
    public void valor_da_mensalidade_nao_pode_ser_nulo() {
        try {
            new Aluno("Nome Aluno", "12345678-9", "111.222.333-44",
                    LocalDateTime.now(), "Rua logo ali", "2111111-11111", null, 10, "Nome responsavel", "21123123");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Valor da mensalidade é obrigatório!");
        }
    }

    @Test
    public void dia_vencimento_mensalidade_nao_pode_ser_nulo() {
        try {
            new Aluno("Nome Aluno", "12345678-9", "111.222.333-44",
                    LocalDateTime.now(), "Rua logo ali", "2111111-11111", 100.0, null, "Nome responsavel", "21123123");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Data de vencimento da mensalidade é obrigatório!");
        }
    }

    @Test
    public void deve_extourar_exceção_caso_for_menor_de_idade() {
        try {
            new Aluno("Nome Aluno", "12345678-9", "111.222.333-44",
                    LocalDateTime.of(2015, 10, 10, 22, 00), "Rua logo ali", "2111111-11111", 100.0, 10, "Nome responsavel", "21123123");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Dados do responsável são obrigatório!");
        }
    }
}

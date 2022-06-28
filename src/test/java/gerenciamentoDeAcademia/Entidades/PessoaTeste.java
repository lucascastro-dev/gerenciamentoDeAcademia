package gerenciamentoDeAcademia.Entidades;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class PessoaTeste {

    @Test
    public void nome_nao_pode_ser_nulo() {
        try {
            new Pessoa(null, "12345678-9", "111.222.333-44",
                    LocalDateTime.now(), "Rua logo ali", "2111111-11111");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Nome é obrigatório!");
        }
    }

    @Test
    public void rg_nao_pode_ser_nulo() {
        try {
            new Pessoa("Nome da Pessoa", null, "111.222.333-44",
                    LocalDateTime.now(), "Rua logo ali", "2111111-11111");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "RG é obrigatório!");
        }
    }

    @Test
    public void cpf_nao_pode_ser_nulo() {
        try {
            new Pessoa("Nome da Pessoa", "12345678-9", null,
                    LocalDateTime.now(), "Rua logo ali", "2111111-11111");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "CPF é obrigatório!");
        }
    }

    @Test
    public void data_de_nascimento_nao_pode_ser_nulo() {
        try {
            new Pessoa("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    null, "Rua logo ali", "2111111-11111");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Data de nascimento é obrigatória!");
        }
    }

    @Test
    public void endereco_nao_pode_ser_nulo() {
        try {
            new Pessoa("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDateTime.now(), null, "2111111-11111");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Endereço é obrigatório!");
        }
    }

    @Test
    public void telefone_nao_pode_ser_nulo() {
        try {
            new Pessoa("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDateTime.now(), "Rua logo ali", null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Telefone é obrigatório!");
        }
    }
}
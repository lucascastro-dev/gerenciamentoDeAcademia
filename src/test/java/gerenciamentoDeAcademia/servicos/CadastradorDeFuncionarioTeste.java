package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Funcionario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class CadastradorDeFuncionarioTeste {
    //TODO: CORRIGIR OS TESTES
    Funcionario funcionario = new Funcionario();

    @Test
    public void deve_cadastrar_aluno() {
        var funcionario = new Funcionario();
        funcionario.setNome("Lucas");
        funcionario.setRg("12345678-9");
        funcionario.setCpf("123.456.789-00");
        funcionario.setDataDeNascimento(LocalDate.of(2000, 12, 20));
        funcionario.setEndereco("Rua do Logo Ali - RJ");
        funcionario.setTelefone("(00) 0000-0000");
        funcionario.setCargo("Professor");
        funcionario.setEspecializacao("Musculação");
        var cadastradorDeFuncionario = new CadastradorDeFuncionario();

        var funcionarioCadastrado = cadastradorDeFuncionario.cadastrar(funcionario);

        Assertions.assertEquals(funcionario.getNome(), funcionarioCadastrado.getNome());
        Assertions.assertEquals(funcionario.getRg(), funcionarioCadastrado.getRg());
        Assertions.assertEquals(funcionario.getCpf(), funcionarioCadastrado.getCpf());
        Assertions.assertEquals(funcionario.getDataDeNascimento(), funcionarioCadastrado.getDataDeNascimento());
        Assertions.assertEquals(funcionario.getEndereco(), funcionarioCadastrado.getEndereco());
        Assertions.assertEquals(funcionario.getTelefone(), funcionarioCadastrado.getTelefone());
        Assertions.assertEquals(funcionario.getCargo(), funcionarioCadastrado.getCargo());
        Assertions.assertEquals(funcionario.getEspecializacao(), funcionarioCadastrado.getEspecializacao());
    }

    @Test
    public void nome_nao_pode_ser_nulo() {
        try {
            new Funcionario(null, "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", "Professor", "Lutas");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Nome é obrigatório!");
        }
    }

    @Test
    public void rg_nao_pode_ser_nulo() {
        try {
            new Funcionario("Nome da Pessoa", null, "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", "Professor", "Lutas");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "RG é obrigatório!");
        }
    }

    @Test
    public void cpf_nao_pode_ser_nulo() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", null,
                    LocalDate.now(), "Rua logo ali", "2111111-11111", "Professor", "Lutas");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "CPF é obrigatório!");
        }
    }

    @Test
    public void data_de_nascimento_nao_pode_ser_nulo() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    null, "Rua logo ali", "2111111-11111", "Professor", "Lutas");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Data de nascimento é obrigatória!");
        }
    }

    @Test
    public void endereco_nao_pode_ser_nulo() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDate.now(), null, "2111111-11111", "Professor", "Lutas");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Endereço é obrigatório!");
        }
    }

    @Test
    public void telefone_nao_pode_ser_nulo() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", null, "Professor", "Lutas");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Telefone é obrigatório!");
        }
    }

    @Test
    public void cargo_nao_pode_ser_nulo() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", null, null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Cargo é obrigatório!");
        }
    }

    @Test
    public void especializacao_eh_obrigatoria_se_for_professor() {
        try {
            new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", "Professor", null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Especialização é obrigatória para professor!");
        }
    }

    @Test
    public void nao_deve_estourar_excecao_se_cargo_for_diferente_de_professor() {
        Assertions.assertDoesNotThrow(() -> new Funcionario("Nome da Pessoa", "12345678-9", "111.222.333-44",
                LocalDate.now(), "Rua logo ali", "2111111-11111", "Estagiário", null));
    }
}
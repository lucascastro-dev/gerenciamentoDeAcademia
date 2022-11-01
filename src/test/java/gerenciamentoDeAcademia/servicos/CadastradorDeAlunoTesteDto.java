package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.AlunoDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class CadastradorDeAlunoTesteDto {
    //TODO: CORRIGIR OS TESTES
    @Test
    public void deve_cadastrar_aluno() {
        var aluno = new AlunoDto();
        aluno.setNome("Lucas");
        aluno.setRg("12345678-9");
        aluno.setCpf("123.456.789-00");
        aluno.setDataDeNascimento(LocalDate.of(2000, 12, 20));
        aluno.setEndereco("Rua do Logo Ali - RJ");
        aluno.setTelefone("(00) 0000-0000");
        aluno.setValorMensalidade(99.90);
        aluno.setDiaVencimentoMensalidade(10);
        aluno.setNomeResponsavel("Nome do Responsável");
        aluno.setTelefoneResponsavel("(00) 0000-0000");
        var cadastradorDeAluno = new CadastradorDeAluno();

        var alunoCadastrado = cadastradorDeAluno.cadastrar(aluno);

        Assertions.assertEquals(aluno.getNome(), alunoCadastrado.getNome());
        Assertions.assertEquals(aluno.getRg(), alunoCadastrado.getRg());
        Assertions.assertEquals(aluno.getCpf(), alunoCadastrado.getCpf());
        Assertions.assertEquals(aluno.getDataDeNascimento(), alunoCadastrado.getDataDeNascimento());
        Assertions.assertEquals(aluno.getEndereco(), alunoCadastrado.getEndereco());
        Assertions.assertEquals(aluno.getTelefone(), alunoCadastrado.getTelefone());
        Assertions.assertEquals(aluno.getValorMensalidade(), alunoCadastrado.getValorMensalidade());
        Assertions.assertEquals(aluno.getDiaVencimentoMensalidade(), alunoCadastrado.getDiaVencimentoMensalidade());
        Assertions.assertEquals(aluno.getNomeResponsavel(), alunoCadastrado.getNomeResponsavel());
        Assertions.assertEquals(aluno.getTelefoneResponsavel(), alunoCadastrado.getTelefoneResponsavel());
    }

    @Test
    public void nome_nao_pode_ser_nulo() {
        try {
            new AlunoDto(null, "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", 100.0, 10, "Lucas", "0000000");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Nome é obrigatório!");
        }
    }

    @Test
    public void rg_nao_pode_ser_nulo() {
        try {
            new AlunoDto("Nome da Pessoa", null, "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", 100.0, 10, "Lucas", "0000000");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "RG é obrigatório!");
        }
    }

    @Test
    public void cpf_nao_pode_ser_nulo() {
        try {
            new AlunoDto("Nome da Pessoa", "12345678-9", null,
                    LocalDate.now(), "Rua logo ali", "2111111-11111", 100.0, 10, "Lucas", "0000000");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "CPF é obrigatório!");
        }
    }

    @Test
    public void data_de_nascimento_nao_pode_ser_nulo() {
        try {
            new AlunoDto("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    null, "Rua logo ali", "2111111-11111", 100.0, 10, "Lucas", "0000000");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Data de nascimento é obrigatória!");
        }
    }

    @Test
    public void endereco_nao_pode_ser_nulo() {
        try {
            new AlunoDto("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDate.now(), null, "2111111-11111", 100.0, 10, "Lucas", "0000000");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Endereço é obrigatório!");
        }
    }

    @Test
    public void telefone_nao_pode_ser_nulo() {
        try {
            new AlunoDto("Nome da Pessoa", "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", null, 100.0, 10, "Lucas", "0000000");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Telefone é obrigatório!");
        }
    }

    @Test
    public void valor_da_mensalidade_nao_pode_ser_nulo() {
        try {
            new AlunoDto("Nome Aluno", "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", null, 10, "Nome responsavel", "21123123");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Valor da mensalidade é obrigatório!");
        }
    }

    @Test
    public void dia_vencimento_mensalidade_nao_pode_ser_nulo() {
        try {
            new AlunoDto("Nome Aluno", "12345678-9", "111.222.333-44",
                    LocalDate.now(), "Rua logo ali", "2111111-11111", 100.0, null, "Nome responsavel", "21123123");
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Data de vencimento da mensalidade é obrigatório!");
        }
    }

    @Test
    public void deve_extourar_exceção_caso_for_menor_de_idade() {
        try {
            new AlunoDto("Nome Aluno", "12345678-9", "111.222.333-44",
                    LocalDate.of(2015, 10, 10), "Rua logo ali", "2111111-11111", 100.0, 10, "Lucas", null);
            Assertions.fail();
        } catch (Exception exception) {
            Assertions.assertEquals(exception.getMessage(), "Dados do responsável são obrigatório!");
        }
    }
}
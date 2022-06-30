package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class CadastradorDeAlunoTeste {

    @Test
    public void deve_cadastrar_aluno() {
        var aluno = new Aluno();
        aluno.setNome("Lucas");
        aluno.setRg("12345678-9");
        aluno.setCpf("123.456.789-00");
        aluno.setDataDeNascimento(LocalDate.of(2000,12,20));
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
}
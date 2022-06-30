package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Funcionario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class CadastradorDeFuncionarioTeste {
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
}
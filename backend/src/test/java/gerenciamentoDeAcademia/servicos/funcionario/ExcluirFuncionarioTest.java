package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.instancio.Select.field;

@ExtendWith(SpringExtension.class)
public class ExcluirFuncionarioTest {
    @InjectMocks
    ExcluirFuncionario excluirFuncionario;
    @Mock
    FuncionarioRepository funcionarioRepository;

    @Test
    void deveExcluirFuncionario() {
        Funcionario funcionario = Instancio.of(Funcionario.class).set(field(Funcionario::getCpf), "90714464090").create();
        Mockito.when(funcionarioRepository.findByCpf(funcionario.getCpf())).thenReturn(funcionario);

        excluirFuncionario.excluirCadastro(funcionario.getCpf());

        Mockito.verify(funcionarioRepository).delete(funcionario);
    }

    @Test
    void deveRetornarMensagemDeErroSeNaoInformarCpfParaExcluirFuncionario() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> excluirFuncionario.excluirCadastro(null));

        Assertions.assertEquals("CPF é obrigatório para excluir funcionário da base!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoNaoEncontrarFuncionarioNaBase() {
        String cpf = "123456";
        Mockito.when(funcionarioRepository.findByCpf(cpf)).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> excluirFuncionario.excluirCadastro(cpf));

        Assertions.assertEquals("Funcionário não encontrado na base!", mensagemDeErro.getMessage());
    }
}

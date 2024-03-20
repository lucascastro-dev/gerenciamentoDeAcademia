package gerenciamentoDeAcademia.servicos.funcionario;

import antlr.MismatchedCharException;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
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
public class AlteradorDeFuncionarioTest {

    @InjectMocks
    AlteradorDeFuncionario alteradorDeFuncionario;
    @Mock
    FuncionarioRepository funcionarioRepository;

    @Test
    void deveAlterarOsDadosDeUmFuncionario() {
        FuncionarioDto funcionarioNovo = Instancio.of(FuncionarioDto.class)
                .set(field(FuncionarioDto::getCpf), "80430802080")
                .set(field(FuncionarioDto::getNome), "Nome alterado").create();

        alteradorDeFuncionario.alterarFuncionario(funcionarioNovo);

        Mockito.verify(funcionarioRepository).save(Mockito.any(Funcionario.class));
    }

    @Test
    void deveConsultarFuncionarioNaBaseAntesDeAlterar() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getCpf), "80430802080").create();
        Funcionario funcionario = Instancio.of(Funcionario.class).set(field(Funcionario::getCpf), "80430802080").create();
        Mockito.when(funcionarioRepository.findByCpf(funcionarioDto.getCpf())).thenReturn(funcionario);

        alteradorDeFuncionario.alterarFuncionario(funcionarioDto);

        Mockito.verify(funcionarioRepository).findByCpf(funcionarioDto.getCpf());
    }

    @Test
    void deveRetornarMensagemDeFuncionarioNaoEncontradoNaBaseQuandoCpfNaoEstiverNaBase() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getCpf), "80430802080").create();
        Mockito.when(funcionarioRepository.findByCpf(funcionarioDto.getCpf())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeFuncionario.alterarFuncionario(funcionarioDto));

        Assertions.assertEquals("Funcionário não encontrado na base de dados!", mensagemDeErro.getMessage());
    }
}

package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ConsultaDeFuncionarioTest {

    @InjectMocks
    ConsultaDeFuncionario consultaDeFuncionario;
    @Mock
    FuncionarioRepository funcionarioRepository;

    @Test
    void deveConsultarTodosOsFuncionarios() {
        consultaDeFuncionario.listarFuncionarios();

        Mockito.verify(funcionarioRepository).findAll();
    }

    @Test
    void deveConsultarUmFuncionarioPeloCpf() {
        String cpf = "12345678900";
        Mockito.when(funcionarioRepository.findByCpf(cpf)).thenReturn(any(Funcionario.class));

        consultaDeFuncionario.consultarFuncionarioPorCpf(cpf);

        Mockito.verify(funcionarioRepository).findByCpf(cpf);
    }

    @Test
    void deveRetornarMensagemDeErroAoInformarCpfNuloNaConsultaPorCpf() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> consultaDeFuncionario.consultarFuncionarioPorCpf(null));

        Assertions.assertEquals("CPF obrigatório para consultar funcionário!", mensagemDeErro.getMessage());
    }

    @Test
    void deveConsultarLogsDoIdDoFuncionario() {
        //TODO: CRIAR TESTES PARA O ENVERS
    }
}

package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ConsultaDeAlunosTest {

    @InjectMocks
    ConsultaDeAlunos consultaDeAlunos;
    @Mock
    AlunoRepository alunoRepository;

    @Test
    void deveConsultarAlunos(){
        consultaDeAlunos.listarAlunos();

        Mockito.verify(alunoRepository).findAll();
    }

    @Test
    void deveConsultarUmAlunoPeloCpf(){
        Aluno alunoEncontrado = Instancio.of(Aluno.class).create();
        String cpf = "123456";
        Mockito.when(alunoRepository.findByCpf(cpf)).thenReturn(alunoEncontrado);

        consultaDeAlunos.consultaAlunoPorCpf(cpf);

        Mockito.verify(alunoRepository).findByCpf(cpf);
    }

    @Test
    void deveRetornarMensagemAlunoNaoEncontrado(){
        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> consultaDeAlunos.consultaAlunoPorCpf("123456"));

        Assertions.assertEquals("Aluno não encontrado na base!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeCpfObrigatorioAoConsultarAlunoComCpfNull(){
        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> consultaDeAlunos.consultaAlunoPorCpf(null));

        Assertions.assertEquals("CPF obrigatório para consulta do aluno!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeCpfObrigatorioAoConsultarAlunoComCpfVazio(){
        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> consultaDeAlunos.consultaAlunoPorCpf(""));

        Assertions.assertEquals("CPF obrigatório para consulta do aluno!", excecao.getMessage());
    }
}

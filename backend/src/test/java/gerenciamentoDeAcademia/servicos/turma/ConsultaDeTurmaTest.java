package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
public class ConsultaDeTurmaTest {

    @InjectMocks
    ConsultaDeTurma consultaDeTurma;
    @Mock
    TurmaRepository turmaRepository;

    @Test
    void deveRetornarListaDeTurmas() {
        consultaDeTurma.listarTurmas();

        Mockito.verify(turmaRepository).findAll();
    }

    @Test
    void deveBuscarTurmaPorId() {
        consultaDeTurma.buscarTurmaPorId(1L);

        Mockito.verify(turmaRepository).findById(anyLong());
    }

    @Test
    void deveRetornarUmaMensagemDeErroQuandoNaoInformarIdNaConsultaPorId() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> consultaDeTurma.buscarTurmaPorId(null));

        Assertions.assertEquals("ID obrigatório para consulta da turma", mensagemDeErro.getMessage());
    }

    @Test
    void deveBuscarTurmaPorModalidade() {
        consultaDeTurma.buscarTurmaPorModalidade("modalidade");

        Mockito.verify(turmaRepository).findByModalidade(anyString());
    }

    @Test
    void deveRetornarUmaMensagemDeErroQuandoNaoInformarModalidadeNaConsultaPorModalidade() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> consultaDeTurma.buscarTurmaPorModalidade(null));

        Assertions.assertEquals("Modalidade obrigatória para consulta da turma", mensagemDeErro.getMessage());
    }
}

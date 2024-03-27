package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ExcluirTurmaTest {

    @InjectMocks
    ExluirTurma excluirTurma;
    @Mock
    TurmaRepository turmaRepository;

    private Long idTurma = 1L;

    @BeforeEach
    void init() {
        Turma turma = Instancio.of(Turma.class).create();
        Optional<Turma> turmaEncontrada = Optional.of(turma);
        Mockito.when(turmaRepository.findById(idTurma)).thenReturn(turmaEncontrada);
    }

    @Test
    void deveExcluirUmaTurma() {
        excluirTurma.excluir(idTurma);

        Mockito.verify(turmaRepository).delete(any());
    }

    @Test
    void deveConsultarTurmaInformadaParaExluir() {
        excluirTurma.excluir(idTurma);

        Mockito.verify(turmaRepository).findById(idTurma);
    }

    @Test
    void deveRetornarMensagemDeErroSeNaoInformarIdParaExcluirTurma() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> excluirTurma.excluir(null));

        Assertions.assertEquals("ID obrigatório para excluir uma turma", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroQuandoTurmaNaoEncontrada() {
        Mockito.when(turmaRepository.findById(idTurma)).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> excluirTurma.excluir(idTurma));

        Assertions.assertEquals("Turma não encontrada na base", mensagemDeErro.getMessage());
    }
}

package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
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

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
public class MontadorDeTurmaTest {

    @InjectMocks
    MontadorDeTurma montadorDeTurma;
    @Mock
    TurmaRepository turmaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    FuncionarioRepository funcionarioRepository;

    private TurmaDto turmaDto;
    private Aluno aluno;
    private Funcionario funcionario;
    private String cpfValido = "36305895023";

    @BeforeEach
    void init() {
        turmaDto = Instancio.of(TurmaDto.class).create();
        aluno = Instancio.of(Aluno.class).set(field(Aluno::getCpf), cpfValido).create();
        funcionario = Instancio.of(Funcionario.class).set(field(Funcionario::getCpf), cpfValido).create();

        Mockito.when(alunoRepository.findByCpf(anyString())).thenReturn(aluno);
        Mockito.when(funcionarioRepository.findByCpf(anyString())).thenReturn(funcionario);
    }

    @Test
    void deveMontarUmaTurmaComSucesso() {
        montadorDeTurma.montar(turmaDto);

        Mockito.verify(turmaRepository).save(Mockito.any(Turma.class));
    }

    @Test
    void deveRetornarMensagemDeErroQuandoFuncionarioPraCadastrarNaTurmaNaoForEncontradoNaBase() {
        Mockito.when(funcionarioRepository.findByCpf(anyString())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> montadorDeTurma.montar(turmaDto));

        Assertions.assertEquals("Funcionario não encontrado", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroQuandoAlunoPraCadastrarNaTurmaNaoForEncontradoNaBase() {
        Mockito.when(alunoRepository.findByCpf(anyString())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> montadorDeTurma.montar(turmaDto));

        Assertions.assertEquals("Aluno não encontrado", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroQuandoNaoInformarModalidadeDaTurma() {
        TurmaDto turmaDto = Instancio.of(TurmaDto.class).set(field(TurmaDto::getModalidade), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> montadorDeTurma.montar(turmaDto));

        Assertions.assertEquals("Modalidade da turma é obrigatória", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroQuandoNaoInformarHorarioDaTurma() {
        TurmaDto turmaDto = Instancio.of(TurmaDto.class).set(field(TurmaDto::getHorario), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> montadorDeTurma.montar(turmaDto));

        Assertions.assertEquals("Horário da turma é obrigatório", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroQuandoNaoInformarDiasDaTurma() {
        TurmaDto turmaDto = Instancio.of(TurmaDto.class).set(field(TurmaDto::getDias), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> montadorDeTurma.montar(turmaDto));

        Assertions.assertEquals("Dias de aula são obrigatórios", mensagemDeErro.getMessage());
    }
}

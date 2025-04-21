package gerenciamentoDeAcademia.servicos.turma;

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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class AlteradorDeTurmaTest {

    @InjectMocks
    AlteradorDeTurma alteradorDeTurma;
    @Mock
    TurmaRepository turmaRepository;
    @Mock
    FuncionarioRepository funcionarioRepository;
    @Mock
    AlunoRepository alunoRepository;

    private Turma turmaParaAlterar;
    private Turma turma;
    private Funcionario professor;
    private Set<Aluno> listaAlunos = new HashSet<>();
    private Aluno aluno1;
    private Aluno aluno2;

    @BeforeEach
    void init() {
        professor = Instancio.of(Funcionario.class).create();
        aluno1 = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "89914289088").create();
        aluno2 = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "86816316088").create();
        listaAlunos.add(aluno1);
        listaAlunos.add(aluno2);
        turmaParaAlterar = Instancio.of(Turma.class)
                .set(field(Turma::getAlunos), listaAlunos)
                .set(field(Turma::getModalidade), "Modalidade").create();
        turma = Instancio.of(Turma.class).set(field(Turma::getModalidade), "Modalidade").create();

        Mockito.when(turmaRepository.findById(turmaParaAlterar.getId()))
                .thenReturn(Optional.ofNullable(turma));
        Mockito.when(funcionarioRepository.findByCpf(turmaParaAlterar.getProfessor().getCpf())).thenReturn(professor);
        Mockito.when(alunoRepository.findByCpf(aluno1.getCpf())).thenReturn(aluno1);
        Mockito.when(alunoRepository.findByCpf(aluno2.getCpf())).thenReturn(aluno2);
    }

    @Test
    void deveAlterarUmaTurmaComSucesso() {
        alteradorDeTurma.alterarTurma(turmaParaAlterar);

        Mockito.verify(turmaRepository).save(any(Turma.class));
    }

    @Test
    void deveAdicionarUmAlunoATurmaComSucesso() {
        alteradorDeTurma.adicionarAlunoNaTurma(turmaParaAlterar);

        Mockito.verify(turmaRepository).save(any(Turma.class));
    }

    @Test
    void deveRemoverUmAlunoATurmaComSucesso() {
        alteradorDeTurma.adicionarAlunoNaTurma(turmaParaAlterar);

        Mockito.verify(turmaRepository).save(any(Turma.class));
    }

    @Test
    void deveConsultarSeTurmaExisteAntesDeAlterar() {
        alteradorDeTurma.alterarTurma(turmaParaAlterar);

        Mockito.verify(turmaRepository).findById(turmaParaAlterar.getId());
    }

    @Test
    void deveRetornarMensagemDeErroSeTurmaNaoEncontrada() {
        Mockito.when(turmaRepository.findById(turmaParaAlterar.getId())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeTurma.alterarTurma(turmaParaAlterar));

        Assertions.assertEquals("Turma não encontrada na base", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroSeAlterarModalidadeDaTurma() {
        turmaParaAlterar.setModalidade("Modalidade alterada");

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeTurma.alterarTurma(turmaParaAlterar));

        Assertions.assertEquals("Não é possível alterar a modalidade da turma", mensagemDeErro.getMessage());
    }

    @Test
    void deveConsultarProfessorNaBaseCasoProfessorSejaDiferenteDaTurmaEncontrada() {
        turmaParaAlterar.getProfessor().setCpf("07757569036");
        Mockito.when(funcionarioRepository.findByCpf(turmaParaAlterar.getProfessor().getCpf())).thenReturn(professor);

        alteradorDeTurma.alterarTurma(turmaParaAlterar);

        Mockito.verify(funcionarioRepository).findByCpf(turmaParaAlterar.getProfessor().getCpf());
    }

    @Test
    void deveRetornarMensagemDeErroSeNaoEncontrarProfessorNaBase() {
        turmaParaAlterar.getProfessor().setCpf("07757569036");
        Mockito.when(funcionarioRepository.findByCpf(turmaParaAlterar.getProfessor().getCpf())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeTurma.alterarTurma(turmaParaAlterar));

        Assertions.assertEquals("Professor não encontrado na base", mensagemDeErro.getMessage());
    }

    @Test
    void deveConsultarAlunoNaBaseEnquantoTiverAlunosAoAdicionar() {
        alteradorDeTurma.adicionarAlunoNaTurma(turmaParaAlterar);

        Mockito.verify(alunoRepository).findByCpf(aluno1.getCpf());
        Mockito.verify(alunoRepository).findByCpf(aluno2.getCpf());
    }

    @Test
    void deveRetornarMensagemDeErroSeAlunoNaoEncontradoNaBaseAoAdicionarAluno() {
        Mockito.when(alunoRepository.findByCpf(aluno1.getCpf())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeTurma.adicionarAlunoNaTurma(turmaParaAlterar));

        Assertions.assertEquals("Aluno não encontrado na base", mensagemDeErro.getMessage());
    }


    @Test
    void deveConsultarAlunoNaBaseEnquantoTiverAlunosAoRemover() {
        turma.getAlunos().add(aluno1);
        turma.getAlunos().add(aluno2);

        alteradorDeTurma.removerAlunoNaTurma(turmaParaAlterar);

        Mockito.verify(alunoRepository).findByCpf(aluno1.getCpf());
        Mockito.verify(alunoRepository).findByCpf(aluno2.getCpf());
    }

    @Test
    void deveRetornarMensagemDeErroSeAlunoNaoEncontradoNaBaseAoRemoverAluno() {
        Mockito.when(alunoRepository.findByCpf(aluno1.getCpf())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeTurma.removerAlunoNaTurma(turmaParaAlterar));

        Assertions.assertEquals("Aluno não encontrado na base", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroSeAlunoJaMatriculadoNaTurmaAoAdicionarAluno() {
        turma.getAlunos().add(aluno1);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeTurma.adicionarAlunoNaTurma(turmaParaAlterar));

        Assertions.assertEquals(String.format("Aluno %s já matriculado na turma", aluno1.getNome()), mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroSeAlunoNaoMatriculadoNaTurmaAoRemoverAluno() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteradorDeTurma.removerAlunoNaTurma(turmaParaAlterar));

        Assertions.assertEquals("Aluno não matriculado na turma", mensagemDeErro.getMessage());
    }
}

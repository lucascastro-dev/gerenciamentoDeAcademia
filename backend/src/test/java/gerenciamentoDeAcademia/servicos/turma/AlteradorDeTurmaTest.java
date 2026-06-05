package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
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
    @Mock
    InstituicaoRepository instituicaoRepository;

    private Turma turmaParaAlterar;
    private Turma turma;
    private Funcionario professor;
    private Set<Aluno> listaAlunos = new HashSet<>();
    private Aluno aluno1;
    private Aluno aluno2;

    @BeforeEach
    void init() {
        professor = Instancio.of(Funcionario.class)
                .set(field(Funcionario::getTipoFuncionario), TipoFuncionario.PROFESSOR)
                .create();
        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        aluno1 = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "89914289088").create();
        aluno2 = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "86816316088").create();
        listaAlunos.add(aluno1);
        listaAlunos.add(aluno2);
        turmaParaAlterar = Instancio.of(Turma.class)
                .set(field(Turma::getAlunos), listaAlunos)
                .set(field(Turma::getModalidade), "Modalidade").create();
        turma = Instancio.of(Turma.class)
                .set(field(Turma::getModalidade), "Modalidade")
                .set(field(Turma::getInstituicao), instituicao)
                .create();

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
    void deveAlterarModalidadeSalaEHorarioDaTurma() {
        turmaParaAlterar.setModalidade("Modalidade alterada");
        turmaParaAlterar.setSala("Sala 2");
        turmaParaAlterar.setHorario("10:00-11:00");

        alteradorDeTurma.alterarTurma(turmaParaAlterar);

        Mockito.verify(turmaRepository).save(any(Turma.class));
    }

    @Test
    void deveVincularProfessorNaTurmaComSucesso() {
        String cpfProfessor = "07757569036";
        Mockito.when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));
        Mockito.when(funcionarioRepository.findByCpf(cpfProfessor)).thenReturn(professor);
        Mockito.when(instituicaoRepository.existsByCnpjAndFuncionarioCpf(1L, cpfProfessor)).thenReturn(true);

        alteradorDeTurma.vincularProfessor(turma.getId(), cpfProfessor);

        Mockito.verify(funcionarioRepository).findByCpf(cpfProfessor);
        Mockito.verify(turmaRepository).save(turma);
    }

    @Test
    void deveRetornarMensagemDeErroSeNaoEncontrarProfessorNaBase() {
        String cpfProfessor = "07757569036";
        Mockito.when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));
        Mockito.when(funcionarioRepository.findByCpf(cpfProfessor)).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> alteradorDeTurma.vincularProfessor(turma.getId(), cpfProfessor));

        Assertions.assertEquals("Professor não encontrado na base", mensagemDeErro.getMessage());
    }

    @Test
    void deveRemoverProfessorDaTurmaQuandoCpfVazio() {
        turma.setProfessor(professor);
        Mockito.when(turmaRepository.findById(turma.getId())).thenReturn(Optional.of(turma));

        alteradorDeTurma.vincularProfessor(turma.getId(), "");

        Assertions.assertNull(turma.getProfessor());
        Mockito.verify(turmaRepository).save(turma);
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
        turmaParaAlterar.setAlunos(Set.of(aluno1));
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

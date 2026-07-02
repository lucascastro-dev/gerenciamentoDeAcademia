package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.AlunoTurmaProfessorDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
class ServicoTurmaProfessorTest {

    private static final String CPF_PROFESSOR = "61482582007";
    private static final String CPF_ALUNO = "12345678909";

    @Mock
    TurmaRepository turmaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    InstituicaoRepository instituicaoRepository;
    @Mock
    ServicoEscopoProfessor servicoEscopoProfessor;

    private VinculoTurmaAluno vinculoTurmaAluno;
    private ServicoTurmaProfessor servicoTurmaProfessor;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        vinculoTurmaAluno = new VinculoTurmaAluno(alunoRepository);
        servicoTurmaProfessor = new ServicoTurmaProfessor(
                turmaRepository, alunoRepository, instituicaoRepository, servicoEscopoProfessor, vinculoTurmaAluno);
    }

    @Test
    @DisplayName("Dado turma com alunos, Quando listar alunos, Então retorna DTO mascarado")
    void deveListarAlunosComDadosMascarados() {
        Turma turma = turmaComAluno();
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(eq(5L), any())).thenReturn(turma);

        List<AlunoTurmaProfessorDto> alunos = servicoTurmaProfessor.listarAlunos(5L, usuarioProfessor());

        Assertions.assertEquals(1, alunos.size());
        Assertions.assertTrue(alunos.get(0).getCpfMascarado().contains("***"));
        Assertions.assertEquals("Aluno Teste", alunos.get(0).getNome());
    }

    @Test
    @DisplayName("Dado aluno matriculado na instituição, Quando adicionar na turma, Então persiste vínculo")
    void deveAdicionarAlunoMatriculadoNaInstituicao() {
        Turma turma = turmaVazia();
        Aluno aluno = aluno(CPF_ALUNO);
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(eq(5L), any())).thenReturn(turma);
        Mockito.when(alunoRepository.findByCpf(CPF_ALUNO)).thenReturn(aluno);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(CPF_ALUNO, 1L)).thenReturn(true);

        servicoTurmaProfessor.adicionarAluno(5L, CPF_ALUNO, usuarioProfessor());

        Assertions.assertEquals(1, turma.getAlunos().size());
        Mockito.verify(turmaRepository).save(turma);
    }

    @Test
    @DisplayName("Dado aluno sem matrícula na instituição, Quando adicionar na turma, Então rejeita")
    void deveRejeitarAlunoSemMatriculaNaInstituicao() {
        Turma turma = turmaVazia();
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(eq(5L), any())).thenReturn(turma);
        Mockito.when(alunoRepository.findByCpf(CPF_ALUNO)).thenReturn(aluno(CPF_ALUNO));
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(CPF_ALUNO, 1L)).thenReturn(false);

        ExcecaoDeDominio erro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> servicoTurmaProfessor.adicionarAluno(5L, CPF_ALUNO, usuarioProfessor()));

        Assertions.assertTrue(erro.getMessage().contains("matriculado"));
    }

    @Test
    @DisplayName("Dado aluno já na turma, Quando adicionar novamente, Então retorna conflito")
    void deveRejeitarAlunoDuplicadoNaTurma() {
        Turma turma = turmaComAluno();
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(eq(5L), any())).thenReturn(turma);
        Mockito.when(alunoRepository.findByCpf(CPF_ALUNO)).thenReturn(aluno(CPF_ALUNO));
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(CPF_ALUNO, 1L)).thenReturn(true);

        ExcecaoDeDominio erro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> servicoTurmaProfessor.adicionarAluno(5L, CPF_ALUNO, usuarioProfessor()));

        Assertions.assertTrue(erro.getMessage().contains("já pertence"));
    }

    @Test
    @DisplayName("Dado aluno na turma, Quando remover, Então desvincula sem desmatricular")
    void deveRemoverAlunoDaTurma() {
        Aluno aluno = aluno(CPF_ALUNO);
        Turma turma = turmaVazia();
        turma.getAlunos().add(aluno);
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(eq(5L), any())).thenReturn(turma);
        Mockito.when(alunoRepository.findByCpf(CPF_ALUNO)).thenReturn(aluno);

        servicoTurmaProfessor.removerAluno(5L, CPF_ALUNO, usuarioProfessor());

        Assertions.assertTrue(turma.getAlunos().isEmpty());
        Mockito.verify(turmaRepository).save(turma);
    }

    private Turma turmaVazia() {
        Turma turma = new Turma();
        turma.setModalidade("Judô Baby");
        turma.setInstituicao(instituicao());
        turma.setProfessor(professor());
        turma.setAlunos(new HashSet<>());
        ReflectionTestUtils.setField(turma, "id", 5L);
        return turma;
    }

    private Turma turmaComAluno() {
        Turma turma = turmaVazia();
        turma.getAlunos().add(aluno(CPF_ALUNO));
        return turma;
    }

    private Aluno aluno(String cpf) {
        return Instancio.of(Aluno.class)
                .set(field(Aluno::getCpf), cpf)
                .set(field(Aluno::getNome), "Aluno Teste")
                .create();
    }

    private Instituicao instituicao() {
        Instituicao inst = new Instituicao();
        ReflectionTestUtils.setField(inst, "id", 1L);
        return inst;
    }

    private Funcionario professor() {
        return Instancio.of(Funcionario.class).set(field(Funcionario::getCpf), CPF_PROFESSOR).create();
    }

    private UsuarioAutenticado usuarioProfessor() {
        Usuario usuario = Instancio.of(Usuario.class).set(field(Usuario::getLogin), CPF_PROFESSOR).create();
        return new UsuarioAutenticado(
                usuario, professor(), null, 1L,
                SituacaoCobranca.ATIVO, StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO, false, false);
    }
}

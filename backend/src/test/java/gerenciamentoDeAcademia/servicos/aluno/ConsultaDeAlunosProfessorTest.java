package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoConsultaProfessorDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeAcesso;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.instancio.Select.field;

@ExtendWith(SpringExtension.class)
class ConsultaDeAlunosProfessorTest {

    private static final String CPF_ALUNO = "12345678909";

    @InjectMocks
    ConsultaDeAlunos consultaDeAlunos;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    InstituicaoRepository instituicaoRepository;
    @Mock
    TurmaRepository turmaRepository;
    @Mock
    ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @Test
    @DisplayName("Dado aluno matriculado na instituição, Quando professor consulta por CPF, Então retorna dados mascarados")
    void deveConsultarAlunoDaInstituicaoComDadosMascarados() {
        Aluno aluno = Instancio.of(Aluno.class)
                .set(field(Aluno::getCpf), CPF_ALUNO)
                .set(field(Aluno::getNome), "Teste Portal Aluno")
                .set(field(Aluno::getEndereco), "{\"numero\":\"10\",\"cidade\":\"Rio de Janeiro\",\"uf\":\"RJ\"}")
                .create();
        Instituicao inst = new Instituicao();
        inst.setRazaoSocial("Inst Master");
        ReflectionTestUtils.setField(inst, "id", 1L);
        Turma turma = new Turma();
        turma.setModalidade("Judô Baby");
        turma.setHorario("18:30-19:10");
        turma.setInstituicao(inst);

        Mockito.when(alunoRepository.findByCpf(CPF_ALUNO)).thenReturn(aluno);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(CPF_ALUNO, 1L)).thenReturn(true);
        Mockito.when(turmaRepository.findTurmasMatriculadasPorCpf(CPF_ALUNO)).thenReturn(List.of(turma));

        AlunoConsultaProfessorDto dto = consultaDeAlunos.consultaProfessorPorCpf(CPF_ALUNO, usuarioProfessor());

        Assertions.assertEquals("Teste Portal Aluno", dto.getNome());
        Assertions.assertTrue(dto.getCpfMascarado().startsWith("***"));
        Assertions.assertFalse(dto.getEnderecoResumido().isBlank());
        Assertions.assertEquals(1, dto.getTurmasInstituicao().size());
    }

    @Test
    @DisplayName("Dado aluno fora da instituição, Quando professor consulta, Então retorna não encontrado")
    void deveOcultarAlunoForaDaInstituicao() {
        Aluno aluno = Instancio.of(Aluno.class).set(field(Aluno::getCpf), CPF_ALUNO).create();
        Mockito.when(alunoRepository.findByCpf(CPF_ALUNO)).thenReturn(aluno);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(CPF_ALUNO, 1L)).thenReturn(false);

        Assertions.assertThrows(
                ExcecaoDeAcesso.class,
                () -> consultaDeAlunos.consultaProfessorPorCpf(CPF_ALUNO, usuarioProfessor()));
    }

    @Test
    @DisplayName("Dado CPF inexistente, Quando professor consulta, Então retorna não encontrado")
    void deveRetornarNaoEncontradoParaCpfInexistente() {
        Mockito.when(alunoRepository.findByCpf(CPF_ALUNO)).thenReturn(null);

        Assertions.assertThrows(
                ExcecaoDeAcesso.class,
                () -> consultaDeAlunos.consultaProfessorPorCpf(CPF_ALUNO, usuarioProfessor()));
    }

    private UsuarioAutenticado usuarioProfessor() {
        Usuario usuario = Instancio.of(Usuario.class).create();
        Funcionario funcionario = Instancio.of(Funcionario.class).create();
        return new UsuarioAutenticado(
                usuario, funcionario, null, 1L,
                SituacaoCobranca.ATIVO, StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO, false, false);
    }
}

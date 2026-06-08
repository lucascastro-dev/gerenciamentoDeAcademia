package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.PessoaListagemDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultaDeAlunos — listagem resumida")
class ConsultaDeAlunosListagemTest {

    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private InstituicaoRepository instituicaoRepository;
    @Mock
    private TurmaRepository turmaRepository;
    @Mock
    private MatriculaInstituicaoRepository matriculaInstituicaoRepository;
    @Mock
    private ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @InjectMocks
    private ConsultaDeAlunos consultaDeAlunos;

    @Test
    @DisplayName("Dado master logado Quando listar Então retorna todos os alunos com CPF completo")
    void deveListarTodosParaMaster() {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Ana");
        aluno.setCpf("12345678909");
        aluno.setDataDeNascimento(LocalDate.of(2000, 1, 15));
        when(alunoRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(aluno));

        UsuarioAutenticado master = usuarioMaster();

        List<PessoaListagemDto> lista = consultaDeAlunos.listarParaListagem(master);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getCpf()).isEqualTo("12345678909");
        assertThat(lista.get(0).getCpfExibicao()).contains("123");
    }

    @Test
    @DisplayName("Dado professor Quando listar Então mascara CPF na listagem")
    void deveMascararCpfParaProfessor() {
        Aluno aluno = new Aluno();
        aluno.setId(2L);
        aluno.setNome("Bruno");
        aluno.setCpf("61482582007");
        MatriculaInstituicao mat = new MatriculaInstituicao();
        mat.setAluno(aluno);
        when(matriculaInstituicaoRepository.findByInstituicao_IdOrderByAluno_NomeAsc(1L)).thenReturn(List.of(mat));

        UsuarioAutenticado professor = usuarioProfessor();

        List<PessoaListagemDto> lista = consultaDeAlunos.listarParaListagem(professor);

        assertThat(lista.get(0).getCpf()).isNull();
        assertThat(lista.get(0).getCpfExibicao()).startsWith("***");
    }

    private UsuarioAutenticado usuarioMaster() {
        Funcionario f = Funcionario.builder().cpf("00000000191").tipoFuncionario(TipoFuncionario.ADMINISTRADOR).build();
        Usuario u = Usuario.builder().login("00000000191").role(UserRole.ADMIN).build();
        return new UsuarioAutenticado(u, f, null, 0L, SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.NAO_APLICAVEL, true, true);
    }

    private UsuarioAutenticado usuarioProfessor() {
        Funcionario f = Funcionario.builder()
                .cpf("61482582007")
                .tipoFuncionario(TipoFuncionario.PROFESSOR)
                .build();
        Usuario u = Usuario.builder().login("61482582007").role(UserRole.USER).build();
        return new UsuarioAutenticado(u, f, null, 1L, SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO, false, false);
    }
}

package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoConsultaCompletaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
class ConsultaDeAlunosTest {

    private static final String CPF_CENARIO = "12345678909";

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

    @org.junit.jupiter.api.BeforeEach
    void configurarMigracaoLegado() {
        Mockito.when(servicoMatriculaInstituicao.consultarFinanceiro(any(), anyLong()))
                .thenReturn(null);
    }

    @Test
    void deveConsultarAlunosDaInstituicao() {
        consultaDeAlunos.listarAlunos(1L);
        Mockito.verify(alunoRepository).findDistinctByTurma_Instituicao_IdOrderByNomeAsc(1L);
    }

    @Test
    void deveConsultarUmAlunoPeloCpf() {
        Aluno alunoEncontrado = Instancio.of(Aluno.class).create();
        Mockito.when(alunoRepository.findByCpf(CPF_CENARIO)).thenReturn(alunoEncontrado);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(CPF_CENARIO, 1L)).thenReturn(true);

        consultaDeAlunos.consultaAlunoPorCpf(CPF_CENARIO, 1L);

        Mockito.verify(alunoRepository).findByCpf(CPF_CENARIO);
    }

    @Test
    void deveRetornarMensagemAlunoNaoEncontrado() {
        Mockito.when(alunoRepository.findByCpf(CPF_CENARIO)).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> consultaDeAlunos.consultaAlunoPorCpf(CPF_CENARIO, 1L));

        Assertions.assertEquals("Aluno não encontrado na base.", mensagemDeErro.getMessage());
        Mockito.verify(turmaRepository, Mockito.never()).findTurmasMatriculadasPorCpf(anyString());
    }

    @Test
    void deveRetornarMensagemDeCpfObrigatorioAoConsultarAlunoComCpfNull() {
        var mensagemDeErro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> consultaDeAlunos.consultaAlunoPorCpf(null, 1L));

        Assertions.assertEquals("CPF obrigatório com 11 dígitos para consulta do aluno.", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeCpfObrigatorioAoConsultarAlunoComCpfVazio() {
        var mensagemDeErro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> consultaDeAlunos.consultaAlunoPorCpf("", 1L));

        Assertions.assertEquals("CPF obrigatório com 11 dígitos para consulta do aluno.", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemAlunoNaoEncontradoNaConsultaCompleta() {
        Mockito.when(alunoRepository.findByCpf(CPF_CENARIO)).thenReturn(null);

        var erro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> consultaDeAlunos.consultaCompletaPorCpf(CPF_CENARIO, usuarioMaster()));

        Assertions.assertEquals("Aluno não encontrado na base.", erro.getMessage());
        Mockito.verify(turmaRepository, Mockito.never()).findTurmasMatriculadasPorCpf(anyString());
    }

    @Test
    void deveMontarConsultaCompletaParaMasterComTurmas() {
        Aluno aluno = Instancio.of(Aluno.class)
                .set(field(Aluno::getCpf), CPF_CENARIO)
                .set(field(Aluno::getNome), "Teste Portal Aluno")
                .create();

        Instituicao instituicao = new Instituicao();
        instituicao.setRazaoSocial("Castro Team");
        org.springframework.test.util.ReflectionTestUtils.setField(instituicao, "id", 1L);

        Turma turma = new Turma();
        turma.setModalidade("[Demo] Judô — turma portal");
        turma.setHorario("18:00-19:30");
        turma.setSala("Sala 1");
        turma.setInstituicao(instituicao);
        org.springframework.test.util.ReflectionTestUtils.setField(turma, "id", 99L);

        Mockito.when(alunoRepository.findByCpf(CPF_CENARIO)).thenReturn(aluno);
        Mockito.when(turmaRepository.findTurmasMatriculadasPorCpf(CPF_CENARIO)).thenReturn(List.of(turma));

        AlunoConsultaCompletaDto dto = consultaDeAlunos.consultaCompletaPorCpf(CPF_CENARIO, usuarioMaster());

        Assertions.assertEquals(CPF_CENARIO, dto.getCpf());
        Assertions.assertEquals("Teste Portal Aluno", dto.getNome());
        Assertions.assertNotNull(dto.getMatriculas());
        Assertions.assertEquals(1, dto.getMatriculas().size());
        Assertions.assertEquals(1L, dto.getMatriculas().get(0).getInstituicaoId());
        Assertions.assertEquals("Castro Team", dto.getMatriculas().get(0).getRazaoSocial());
        Assertions.assertEquals(1, dto.getMatriculas().get(0).getTurmas().size());
        Assertions.assertEquals("[Demo] Judô — turma portal", dto.getMatriculas().get(0).getTurmas().get(0).getModalidade());

        Mockito.verify(turmaRepository).findTurmasMatriculadasPorCpf(eq(CPF_CENARIO));
    }

    @Test
    void deveRetornarConsultaCompletaSemMatriculasQuandoAlunoNaoTemTurma() {
        Aluno aluno = Instancio.of(Aluno.class).set(field(Aluno::getCpf), CPF_CENARIO).create();
        Mockito.when(alunoRepository.findByCpf(CPF_CENARIO)).thenReturn(aluno);
        Mockito.when(turmaRepository.findTurmasMatriculadasPorCpf(CPF_CENARIO)).thenReturn(List.of());

        AlunoConsultaCompletaDto dto = consultaDeAlunos.consultaCompletaPorCpf(CPF_CENARIO, usuarioMaster());

        Assertions.assertTrue(dto.getMatriculas().isEmpty());
        Mockito.verify(turmaRepository).findTurmasMatriculadasPorCpf(CPF_CENARIO);
    }

    @Test
    void deveFiltrarMatriculasPelaInstituicaoDoUsuarioNaoMaster() {
        Aluno aluno = Instancio.of(Aluno.class).set(field(Aluno::getCpf), CPF_CENARIO).create();
        Instituicao inst1 = new Instituicao();
        inst1.setRazaoSocial("Inst A");
        org.springframework.test.util.ReflectionTestUtils.setField(inst1, "id", 1L);
        Instituicao inst2 = new Instituicao();
        inst2.setRazaoSocial("Inst B");
        org.springframework.test.util.ReflectionTestUtils.setField(inst2, "id", 2L);

        Turma turma1 = turmaComInstituicao("Turma A", inst1);
        Turma turma2 = turmaComInstituicao("Turma B", inst2);

        Mockito.when(alunoRepository.findByCpf(CPF_CENARIO)).thenReturn(aluno);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(CPF_CENARIO, 1L)).thenReturn(true);
        Mockito.when(turmaRepository.findTurmasMatriculadasPorCpf(CPF_CENARIO))
                .thenReturn(List.of(turma1, turma2));

        UsuarioAutenticado usuario = new UsuarioAutenticado(
                Instancio.of(Usuario.class).create(),
                Instancio.of(gerenciamentoDeAcademia.entidades.Funcionario.class).create(),
                null,
                1L,
                gerenciamentoDeAcademia.enums.SituacaoCobranca.ATIVO,
                gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                false,
                false);

        AlunoConsultaCompletaDto dto = consultaDeAlunos.consultaCompletaPorCpf(CPF_CENARIO, usuario);

        Assertions.assertEquals(1, dto.getMatriculas().size());
        Assertions.assertEquals(1L, dto.getMatriculas().get(0).getInstituicaoId());
        Assertions.assertEquals("Inst A", dto.getMatriculas().get(0).getRazaoSocial());
    }

    private static Turma turmaComInstituicao(String modalidade, Instituicao instituicao) {
        Turma turma = new Turma();
        turma.setModalidade(modalidade);
        turma.setHorario("10:00-11:00");
        turma.setInstituicao(instituicao);
        org.springframework.test.util.ReflectionTestUtils.setField(turma, "id", 10L);
        return turma;
    }

    private static UsuarioAutenticado usuarioMaster() {
        return new UsuarioAutenticado(
                Instancio.of(Usuario.class).create(),
                Instancio.of(gerenciamentoDeAcademia.entidades.Funcionario.class).create(),
                null,
                0L,
                gerenciamentoDeAcademia.enums.SituacaoCobranca.ATIVO,
                gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao.NAO_APLICAVEL,
                true,
                true);
    }
}

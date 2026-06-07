package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.PresencaGradeDto;
import gerenciamentoDeAcademia.dto.PresencaSalvarDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.PresencaAluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.StatusPresenca;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.PresencaAlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.instancio.Select.field;

@ExtendWith(SpringExtension.class)
class ServicoPresencaTurmaTest {

    private static final String CPF_PROFESSOR = "61482582007";
    private static final String CPF_ALUNO = "12345678909";

    @InjectMocks
    ServicoPresencaTurma servicoPresencaTurma;
    @Mock
    PresencaAlunoRepository presencaAlunoRepository;
    @Mock
    TurmaRepository turmaRepository;
    @Mock
    GeradorRelatorioPresencaPdf geradorRelatorioPresencaPdf;
    @Mock
    ServicoEscopoProfessor servicoEscopoProfessor;

    @Test
    @DisplayName("Dado turma com Terça e Quinta, Quando montar grade de junho/2026, Então retorna dias de aula")
    void deveCalcularDiasComAulaParaDiasCurtos() {
        Turma turma = turmaComDias(List.of("Terça", "Quinta"));
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(Mockito.eq(5L), Mockito.any()))
                .thenReturn(turma);
        Mockito.when(presencaAlunoRepository.findByTurmaIdAndDataAulaBetween(
                Mockito.eq(5L), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
                .thenReturn(List.of());

        PresencaGradeDto grade = servicoPresencaTurma.montarGrade(5L, 2026, 6, usuarioProfessor());

        Assertions.assertFalse(grade.getDiasComAula().isEmpty());
        Assertions.assertTrue(grade.getDiasComAula().contains(2));
        Assertions.assertTrue(grade.getDiasComAula().contains(4));
    }

    @Test
    @DisplayName("Dado registro de presença, Quando salvar, Então persiste status do aluno")
    void deveSalvarRegistroDePresenca() {
        Turma turma = turmaComDias(List.of("Terça"));
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(Mockito.eq(5L), Mockito.any()))
                .thenReturn(turma);
        Mockito.when(presencaAlunoRepository.findByTurmaIdAndDataAulaBetween(
                Mockito.eq(5L), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
                .thenReturn(List.of());

        PresencaSalvarDto dto = new PresencaSalvarDto();
        dto.setAno(2026);
        dto.setMes(6);
        PresencaSalvarDto.RegistroPresencaDto reg = new PresencaSalvarDto.RegistroPresencaDto();
        reg.setAlunoCpf(CPF_ALUNO);
        reg.setDia(2);
        reg.setStatus("P");
        dto.setRegistros(List.of(reg));

        servicoPresencaTurma.salvar(5L, dto, usuarioProfessor());

        ArgumentCaptor<PresencaAluno> captor = ArgumentCaptor.forClass(PresencaAluno.class);
        Mockito.verify(presencaAlunoRepository).save(captor.capture());
        Assertions.assertEquals(StatusPresenca.P, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Dado grade montada, Quando gerar PDF, Então delega ao gerador")
    void deveGerarPdfDaGrade() {
        Turma turma = turmaComDias(List.of("Terça"));
        turma.setInstituicao(new gerenciamentoDeAcademia.entidades.Instituicao());
        turma.getInstituicao().setRazaoSocial("Inst Master");
        Mockito.when(servicoEscopoProfessor.exigirTurmaDoProfessor(Mockito.eq(5L), Mockito.any()))
                .thenReturn(turma);
        Mockito.when(presencaAlunoRepository.findByTurmaIdAndDataAulaBetween(
                Mockito.eq(5L), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
                .thenReturn(List.of());
        Mockito.when(geradorRelatorioPresencaPdf.gerar(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(new byte[]{1, 2, 3});

        byte[] pdf = servicoPresencaTurma.gerarPdf(5L, 2026, 6, usuarioProfessor());

        Assertions.assertEquals(3, pdf.length);
        Mockito.verify(geradorRelatorioPresencaPdf).gerar(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }

    private Turma turmaComDias(List<String> dias) {
        Aluno aluno = Instancio.of(Aluno.class)
                .set(field(Aluno::getCpf), CPF_ALUNO)
                .set(field(Aluno::getNome), "Aluno Teste")
                .create();
        Funcionario professor = Instancio.of(Funcionario.class)
                .set(field(Funcionario::getCpf), CPF_PROFESSOR)
                .create();
        Turma turma = new Turma();
        turma.setModalidade("Judô Baby");
        turma.setDias(dias);
        turma.setProfessor(professor);
        turma.setAlunos(new HashSet<>(List.of(aluno)));
        ReflectionTestUtils.setField(turma, "id", 5L);
        return turma;
    }

    private UsuarioAutenticado usuarioProfessor() {
        Usuario usuario = Instancio.of(Usuario.class).set(field(Usuario::getLogin), CPF_PROFESSOR).create();
        return new UsuarioAutenticado(
                usuario, Instancio.of(Funcionario.class).create(), null, 1L,
                SituacaoCobranca.ATIVO, StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO, false, false);
    }
}

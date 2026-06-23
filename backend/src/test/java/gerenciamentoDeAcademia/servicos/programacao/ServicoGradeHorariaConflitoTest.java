package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ConflitoHorarioDto;
import gerenciamentoDeAcademia.dto.GradeHorariaEventoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class ServicoGradeHorariaConflitoTest {

    @InjectMocks
    ServicoGradeHoraria servicoGradeHoraria;

    @Mock
    TurmaRepository turmaRepository;

    @Mock
    ItemProgramacaoAlunoRepository programacaoRepository;

    @Test
    @DisplayName("Dado evento da grade sem horário, Quando detectar conflitos, Então não lança erro")
    void deveIgnorarEventosSemHorarioNaDeteccaoDeConflitos() {
        ServicoGradeHoraria spyServico = spy(servicoGradeHoraria);
        GradeHorariaEventoDto eventoSemHorario = new GradeHorariaEventoDto(
                "TURMA",
                1L,
                "Turma teste",
                null,
                null,
                "Judô",
                "Dojo 1",
                "sábado",
                LocalDate.of(2026, 6, 20),
                null,
                null,
                false);
        doReturn(List.of(eventoSemHorario)).when(spyServico).montarGrade(anyLong(), any(LocalDate.class));

        Aluno aluno = new Aluno();
        aluno.setCpf("83025278072");
        aluno.setNome("Aluno teste");
        Instituicao instituicao = new Instituicao();
        ReflectionTestUtils.setField(instituicao, "id", 1L);

        ItemProgramacaoAluno item = ItemProgramacaoAluno.builder()
                .instituicao(instituicao)
                .aluno(aluno)
                .tipo(TipoItemProgramacao.EVENTO)
                .titulo("Exame")
                .dataPrevista(LocalDate.of(2026, 6, 20))
                .horaInicio(LocalTime.of(18, 0))
                .horaFim(LocalTime.of(19, 30))
                .sala("Dojo 1")
                .build();

        assertDoesNotThrow(() -> {
            List<ConflitoHorarioDto> conflitos = spyServico.detectarConflitosItem(1L, item, null);
            org.junit.jupiter.api.Assertions.assertNotNull(conflitos);
        });
    }
}

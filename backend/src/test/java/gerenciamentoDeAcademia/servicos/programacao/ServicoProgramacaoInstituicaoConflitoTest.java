package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import gerenciamentoDeAcademia.dto.ItemProgramacaoFormDto;
import gerenciamentoDeAcademia.dto.SalaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.enums.EscopoLancamentoProgramacao;
import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoProgramacaoInstituicaoConflitoTest {

    @InjectMocks
    ServicoProgramacaoInstituicao servico;

    @Mock
    ItemProgramacaoAlunoRepository repository;

    @Mock
    InstituicaoRepository instituicaoRepository;

    @Mock
    AlunoRepository alunoRepository;

    @Mock
    TurmaRepository turmaRepository;

    @Mock
    ServicoGradeHoraria servicoGradeHoraria;

    @Mock
    ServicoSala servicoSala;

    @Test
    @DisplayName("Dado lista de conflitos vazia, Quando criar programação, Então não lança IndexOutOfBounds")
    void criarSemConflitosNaoLancaErro() {
        Long instituicaoId = 1L;
        Instituicao instituicao = new Instituicao();
        instituicao.setId(instituicaoId);

        Aluno aluno = new Aluno();
        aluno.setCpf("83025278072");
        aluno.setNome("Aluno teste");

        ItemProgramacaoFormDto form = new ItemProgramacaoFormDto();
        form.setEscopoLancamento(EscopoLancamentoProgramacao.ALUNO);
        form.setCpfAluno("83025278072");
        form.setTipo(TipoItemProgramacao.PROVA);
        form.setTitulo("Exame de graduação");
        form.setDataPrevista(LocalDate.of(2026, 6, 20));
        form.setHoraInicio("18:00");
        form.setHoraFim("19:30");
        form.setSala("Dojô Gouveia");

        when(instituicaoRepository.findById(instituicaoId)).thenReturn(Optional.of(instituicao));
        when(alunoRepository.findByCpf("83025278072")).thenReturn(aluno);
        when(servicoSala.listar(instituicaoId)).thenAnswer(inv -> {
            SalaDto sala = new SalaDto();
            sala.setId(1L);
            sala.setNome("Dojô Gouveia");
            return List.of(sala);
        });
        when(servicoGradeHoraria.detectarConflitosItem(eq(instituicaoId), any(ItemProgramacaoAluno.class), eq(null)))
                .thenReturn(List.of());
        when(repository.save(any(ItemProgramacaoAluno.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> {
            ItemProgramacaoDto dto = servico.criar(instituicaoId, form);
            assertNotNull(dto);
        });
    }
}

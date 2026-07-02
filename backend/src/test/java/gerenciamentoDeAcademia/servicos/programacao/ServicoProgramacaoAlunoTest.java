package gerenciamentoDeAcademia.servicos.programacao;

import gerenciamentoDeAcademia.dto.ItemProgramacaoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import gerenciamentoDeAcademia.repositorios.ItemProgramacaoAlunoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoProgramacaoAlunoTest {

  private static final String CPF = "83025278072";
  private static final Long INSTITUICAO_ID = 4L;

  @InjectMocks
  ServicoProgramacaoAluno servico;

  @Mock
  ItemProgramacaoAlunoRepository repository;

  @Test
  void deveIncluirProgramacaoDaTurmaMatriculada() {
    Aluno aluno = new Aluno();
    aluno.setCpf(CPF);
    aluno.setNome("Bernardo");

    ItemProgramacaoAluno individual = item(5L, "Exame de graduação", aluno, null);
    ItemProgramacaoAluno turma = item(13L, "Revisão exame de graduação", null, turmaComAluno(aluno));

    when(repository.findByAluno_CpfAndInstituicao_IdOrderByDataPrevistaAsc(CPF, INSTITUICAO_ID))
        .thenReturn(List.of(individual));
    when(repository.findProgramacaoDasTurmasDoAluno(CPF, INSTITUICAO_ID))
        .thenReturn(List.of(turma));

    List<ItemProgramacaoDto> itens = servico.listarPorAlunoEInstituicao(aluno, INSTITUICAO_ID);

    assertEquals(2, itens.size());
    assertTrue(itens.stream().anyMatch(i -> "Revisão exame de graduação".equals(i.titulo())));
    assertTrue(itens.stream().anyMatch(i -> "Exame de graduação".equals(i.titulo())));
  }

  private static Turma turmaComAluno(Aluno aluno) {
    Turma turma = new Turma();
    turma.setId(7L);
    turma.setModalidade("Judo Juvenil");
    turma.setAlunos(Set.of(aluno));
    return turma;
  }

  private static ItemProgramacaoAluno item(Long id, String titulo, Aluno aluno, Turma turma) {
    return ItemProgramacaoAluno.builder()
        .id(id)
        .tipo(TipoItemProgramacao.AULA)
        .titulo(titulo)
        .dataPrevista(LocalDate.of(2026, 7, 5))
        .aluno(aluno)
        .turma(turma)
        .build();
  }
}

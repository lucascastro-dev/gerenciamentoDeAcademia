package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.DashboardResumoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoVinculoAlunoInstituicao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoDashboardInstituicaoTest {

    @Mock
    private MatriculaInstituicaoRepository matriculaInstituicaoRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private VinculoFuncionarioInstituicaoRepository vinculoFuncionarioInstituicaoRepository;
    @Mock
    private TurmaRepository turmaRepository;

    @InjectMocks
    private ServicoDashboardInstituicao servico;

    @Test
    void deveResumirIndicadoresDaInstituicao() {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        MatriculaInstituicao mat = new MatriculaInstituicao();
        mat.setAluno(aluno);

        Funcionario ativo = Funcionario.builder().cadastroAtivo(true).build();
        VinculoFuncionarioInstituicao vinculo = new VinculoFuncionarioInstituicao();
        vinculo.setFuncionario(ativo);

        Turma turmaReal = new Turma();
        turmaReal.setModalidade("Judô Baby");
        Turma turmaMatricula = new Turma();
        turmaMatricula.setModalidade(ServicoVinculoAlunoInstituicao.MODALIDADE_MATRICULA);

        when(matriculaInstituicaoRepository.findByInstituicao_IdOrderByAluno_NomeAsc(1L)).thenReturn(List.of(mat));
        when(alunoRepository.findDistinctByTurma_Instituicao_IdOrderByNomeAsc(1L)).thenReturn(List.of());
        when(vinculoFuncionarioInstituicaoRepository.findByInstituicaoIdComDetalhes(1L)).thenReturn(List.of(vinculo));
        when(turmaRepository.findByInstituicao_IdComDetalhes(1L)).thenReturn(List.of(turmaReal, turmaMatricula));

        DashboardResumoDto resumo = servico.resumo(1L);

        assertThat(resumo.totalAlunos()).isEqualTo(1);
        assertThat(resumo.funcionariosAtivos()).isEqualTo(1);
        assertThat(resumo.totalTurmas()).isEqualTo(1);
    }
}

package gerenciamentoDeAcademia.servicos.financeiro;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.repositorios.CobrancaExternaRepository;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoMatriculaInstituicao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class ServicoFinanceiroTest {

    @InjectMocks
    ServicoFinanceiro servicoFinanceiro;
    @Mock
    MatriculaInstituicaoRepository matriculaInstituicaoRepository;
    @Mock
    ServicoMatriculaInstituicao servicoMatriculaInstituicao;
    @Mock
    CobrancaExternaRepository cobrancaExternaRepository;

    @Test
    void deveCalcularReceitaPrevista() {
        Aluno a = new Aluno();
        a.setNome("Teste");
        a.setCpf("1");
        MatriculaInstituicao matricula = new MatriculaInstituicao();
        matricula.setAluno(a);
        matricula.setValorMensalidade(100.0);
        matricula.setDiaVencimentoMensalidade(28);
        Mockito.when(matriculaInstituicaoRepository.findByInstituicao_IdOrderByAluno_NomeAsc(1L))
                .thenReturn(List.of(matricula));
        Mockito.when(cobrancaExternaRepository
                .findByInstituicao_IdAndTipoAndMesCompetenciaAndAnoCompetencia(
                        Mockito.anyLong(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of());

        var dash = servicoFinanceiro.obterDashboard(1L);
        assertEquals(100.0, dash.receitaMensalPrevista());
        assertEquals(1, dash.totalAlunos());
    }
}

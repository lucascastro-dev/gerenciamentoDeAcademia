package gerenciamentoDeAcademia.servicos.financeiro;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
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
    AlunoRepository alunoRepository;

    @Test
    void deveCalcularReceitaPrevista() {
        Aluno a = new Aluno();
        a.setNome("Teste");
        a.setCpf("1");
        a.setValorMensalidade(100.0);
        a.setDiaVencimentoMensalidade(28);
        Mockito.when(alunoRepository.findAll()).thenReturn(List.of(a));

        var dash = servicoFinanceiro.obterDashboard();
        assertEquals(100.0, dash.receitaMensalPrevista());
        assertEquals(1, dash.totalAlunos());
    }
}

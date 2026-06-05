package gerenciamentoDeAcademia.servicos.cobranca;

import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.repositorios.AssinaturaPlataformaRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoMatriculaInstituicao;
import gerenciamentoDeAcademia.servicos.financeiro.ServicoFinanceiro;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class ServicoSituacaoCobrancaTest {

    @InjectMocks
    ServicoSituacaoCobranca servico;

    @Mock
    AssinaturaPlataformaRepository assinaturaRepository;

    @Mock
    ServicoFinanceiro servicoFinanceiro;

    @Mock
    ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @Test
    void deveBloquearLoginQuandoInstituicaoNaoTemAssinatura() {
        Mockito.when(assinaturaRepository.findByInstituicao_Id(6L)).thenReturn(Optional.empty());

        Assertions.assertEquals(SituacaoCobranca.BLOQUEADO, servico.situacaoPlanoInstituicao(6L));
    }

    @Test
    void devePermitirLoginComPlanoVigente() {
        ReflectionTestUtils.setField(servico, "diasTolerancia", 5);
        AssinaturaPlataforma assinatura = assinaturaVigente(PlanoInstituicaoTipo.MENSAL);
        Mockito.when(assinaturaRepository.findByInstituicao_Id(6L)).thenReturn(Optional.of(assinatura));

        Assertions.assertEquals(SituacaoCobranca.ATIVO, servico.situacaoPlanoInstituicao(6L));
    }

    @Test
    void deveBloquearLoginComPlanoExpiradoForaDaTolerancia() {
        ReflectionTestUtils.setField(servico, "diasTolerancia", 5);
        LocalDate hoje = LocalDate.now();
        AssinaturaPlataforma assinatura = AssinaturaPlataforma.builder()
                .instituicao(new Instituicao())
                .plano(PlanoInstituicaoTipo.MENSAL)
                .dataInicio(hoje.minusDays(60))
                .dataFim(hoje.minusDays(10))
                .ativo(true)
                .build();
        Mockito.when(assinaturaRepository.findByInstituicao_Id(6L)).thenReturn(Optional.of(assinatura));

        Assertions.assertEquals(SituacaoCobranca.BLOQUEADO, servico.situacaoPlanoInstituicao(6L));
    }

    private static AssinaturaPlataforma assinaturaVigente(PlanoInstituicaoTipo tipo) {
        LocalDate hoje = LocalDate.now();
        return AssinaturaPlataforma.builder()
                .instituicao(new Instituicao())
                .plano(tipo)
                .dataInicio(hoje)
                .dataFim(tipo.calcularFim(hoje))
                .ativo(true)
                .build();
    }
}

package gerenciamentoDeAcademia.servicos.plano;

import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AssinaturaPlataformaRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicoAssinaturaPlataformaTest {

    @InjectMocks
    ServicoAssinaturaPlataforma servico;

    @Mock
    AssinaturaPlataformaRepository assinaturaRepository;

    @Mock
    InstituicaoRepository instituicaoRepository;

    @Test
    void deveRejeitarTrialQuandoJaUtilizado() {
        Instituicao instituicao = new Instituicao();
        instituicao.setTrialUtilizado(true);

        ExcecaoDeDominio erro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> servico.validarTrialDisponivel(instituicao, PlanoInstituicaoTipo.TRIAL_7_DIAS));

        Assertions.assertTrue(erro.getMessage().contains("teste grátis"));
    }

    @Test
    void devePermitirTrialQuandoNaoUtilizado() {
        Instituicao instituicao = new Instituicao();
        instituicao.setTrialUtilizado(false);

        Assertions.assertDoesNotThrow(
                () -> servico.validarTrialDisponivel(instituicao, PlanoInstituicaoTipo.TRIAL_7_DIAS));
    }
}

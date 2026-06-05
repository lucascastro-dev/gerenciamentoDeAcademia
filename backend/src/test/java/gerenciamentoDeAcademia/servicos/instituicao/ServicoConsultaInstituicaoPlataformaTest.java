package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.AssinaturaPlataformaDto;
import gerenciamentoDeAcademia.dto.AtualizarPlanoInstituicaoRequest;
import gerenciamentoDeAcademia.dto.InstituicaoDetalheDto;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ServicoConsultaInstituicaoPlataformaTest {

    private static final String CNPJ_CASTRO = "23498897000120";

    @InjectMocks
    ServicoConsultaInstituicaoPlataforma servico;

    @Mock
    InstituicaoRepository instituicaoRepository;

    @Mock
    ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    @Test
    void deveAtualizarPlanoQuandoInstituicaoAtiva() {
        Instituicao instituicao = instituicaoAtiva(6L);
        AssinaturaPlataformaDto assinaturaDto = new AssinaturaPlataformaDto();
        assinaturaDto.setPlano(PlanoInstituicaoTipo.MENSAL);
        assinaturaDto.setVigente(true);

        Mockito.when(instituicaoRepository.findByCnpj(CNPJ_CASTRO)).thenReturn(instituicao);
        Mockito.when(servicoAssinaturaPlataforma.ativarPlano(Mockito.eq(instituicao), Mockito.eq(PlanoInstituicaoTipo.MENSAL)))
                .thenReturn(assinaturaDto);

        AtualizarPlanoInstituicaoRequest request = new AtualizarPlanoInstituicaoRequest();
        request.setCnpj(CNPJ_CASTRO);
        request.setPlano(PlanoInstituicaoTipo.MENSAL);

        InstituicaoDetalheDto resultado = servico.atualizarPlanoPorCnpj(request);

        Assertions.assertNotNull(resultado);
        Assertions.assertTrue(resultado.getCadastroAtivo());
        Assertions.assertNotNull(resultado.getAssinatura());
        Assertions.assertEquals(PlanoInstituicaoTipo.MENSAL, resultado.getAssinatura().getPlano());
        Mockito.verify(servicoAssinaturaPlataforma).ativarPlano(instituicao, PlanoInstituicaoTipo.MENSAL);
    }

    @Test
    void deveExigirCadastroAtivoParaDefinirPlano() {
        Instituicao instituicao = instituicaoAtiva(6L);
        instituicao.setCadastroAtivo(false);
        Mockito.when(instituicaoRepository.findByCnpj(CNPJ_CASTRO)).thenReturn(instituicao);

        AtualizarPlanoInstituicaoRequest request = new AtualizarPlanoInstituicaoRequest();
        request.setCnpj(CNPJ_CASTRO);
        request.setPlano(PlanoInstituicaoTipo.ANUAL);

        ExcecaoDeDominio erro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> servico.atualizarPlanoPorCnpj(request));

        Assertions.assertTrue(erro.getMessage().contains("Ative o cadastro"));
        Mockito.verify(servicoAssinaturaPlataforma, Mockito.never())
                .ativarPlano(Mockito.any(Instituicao.class), Mockito.any());
    }

    @Test
    void deveBloquearTrialQuandoJaUtilizado() {
        Instituicao instituicao = instituicaoAtiva(6L);
        instituicao.setTrialUtilizado(true);
        Mockito.when(instituicaoRepository.findByCnpj(CNPJ_CASTRO)).thenReturn(instituicao);
        Mockito.doAnswer(inv -> {
            Instituicao inst = inv.getArgument(0);
            PlanoInstituicaoTipo plano = inv.getArgument(1);
            if (plano == PlanoInstituicaoTipo.TRIAL_7_DIAS
                    && Boolean.TRUE.equals(inst.getTrialUtilizado())) {
                throw new ExcecaoDeDominio(
                        "O teste grátis de 7 dias já foi utilizado por esta instituição.");
            }
            return new AssinaturaPlataformaDto();
        }).when(servicoAssinaturaPlataforma)
                .ativarPlano(Mockito.eq(instituicao), Mockito.eq(PlanoInstituicaoTipo.TRIAL_7_DIAS));

        AtualizarPlanoInstituicaoRequest request = new AtualizarPlanoInstituicaoRequest();
        request.setCnpj(CNPJ_CASTRO);
        request.setPlano(PlanoInstituicaoTipo.TRIAL_7_DIAS);

        ExcecaoDeDominio erro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> servico.atualizarPlanoPorCnpj(request));

        Assertions.assertTrue(erro.getMessage().contains("teste grátis"));
    }

    private static Instituicao instituicaoAtiva(Long id) {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(id);
        instituicao.setCnpj(CNPJ_CASTRO);
        instituicao.setRazaoSocial("Castro Team");
        instituicao.setCadastroAtivo(true);
        return instituicao;
    }
}

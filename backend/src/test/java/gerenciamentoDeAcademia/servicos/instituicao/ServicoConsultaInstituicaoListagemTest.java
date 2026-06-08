package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.InstituicaoListagemDto;
import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.PlanoInstituicaoTipo;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.repositorios.AssinaturaPlataformaRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.servicos.plano.ServicoAssinaturaPlataforma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServicoConsultaInstituicaoPlataforma — listagem resumida")
class ServicoConsultaInstituicaoListagemTest {

    @Mock
    private InstituicaoRepository instituicaoRepository;
    @Mock
    private AssinaturaPlataformaRepository assinaturaRepository;
    @Mock
    private ServicoAssinaturaPlataforma servicoAssinaturaPlataforma;

    @InjectMocks
    private ServicoConsultaInstituicaoPlataforma servico;

    @Test
    @DisplayName("Quando listar Então retorna instituições com plano e status")
    void deveListarTodasInstituicoes() {
        Instituicao b = new Instituicao();
        b.setId(2L);
        b.setRazaoSocial("Beta Academia");
        b.setCnpj("23498897000120");
        b.setCadastroAtivo(true);
        b.setStatusFinanceiro(StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO);
        Instituicao a = new Instituicao();
        a.setId(1L);
        a.setRazaoSocial("Alpha Escola");
        a.setCnpj("11222333000181");
        a.setCadastroAtivo(false);
        a.setStatusFinanceiro(StatusFinanceiroInstituicao.PENDENTE_PAGAMENTO);
        when(instituicaoRepository.findAll()).thenReturn(List.of(b, a));

        AssinaturaPlataforma assinatura = AssinaturaPlataforma.builder()
                .instituicao(b)
                .plano(PlanoInstituicaoTipo.MENSAL)
                .build();
        when(assinaturaRepository.findAllComInstituicao()).thenReturn(List.of(assinatura));

        List<InstituicaoListagemDto> lista = servico.listarParaListagem();

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getRazaoSocial()).isEqualTo("Alpha Escola");
        assertThat(lista.get(0).getStatusFinanceiroExibicao()).isEqualTo("Pagamento pendente");
        assertThat(lista.get(0).getStatusCadastroExibicao()).isEqualTo("Inativo");
        assertThat(lista.get(0).getPlanoExibicao()).isEqualTo("—");
        assertThat(lista.get(1).getRazaoSocial()).isEqualTo("Beta Academia");
        assertThat(lista.get(1).getCnpjExibicao()).isEqualTo("23.498.897/0001-20");
        assertThat(lista.get(1).getPlanoExibicao()).isEqualTo("Mensal");
        assertThat(lista.get(1).getStatusCadastroExibicao()).isEqualTo("Ativo");
    }
}

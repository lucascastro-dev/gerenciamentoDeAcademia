package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioConsultaCompletaDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultaDeFuncionario — escopo por instituição")
class ConsultaDeFuncionarioEscopoTest {

    private static final String CPF_PRE_CADASTRO = "52998224725";
    private static final Long INSTITUICAO_CASTRO = 2L;

    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private InstituicaoRepository instituicaoRepository;
    @Mock
    private VinculoFuncionarioInstituicaoRepository vinculoRepository;

    @InjectMocks
    private ConsultaDeFuncionario consultaDeFuncionario;

    @Test
    @DisplayName("Dado RH com funcionario:ativar, Quando consultar pré-cadastro sem vínculo, Então retorna dados")
    void rhDeveConsultarPreCadastroSemVinculo() {
        Funcionario preCadastro = Funcionario.builder()
                .cpf(CPF_PRE_CADASTRO)
                .nome("Pré-cadastro Teste")
                .build();
        when(funcionarioRepository.findByCpf(CPF_PRE_CADASTRO)).thenReturn(preCadastro);
        when(vinculoRepository.findByFuncionarioCpfOrderByInstituicaoRazaoSocialAsc(CPF_PRE_CADASTRO))
                .thenReturn(List.of());

        UsuarioAutenticado rh = usuarioInstitucional(TipoFuncionario.RH, INSTITUICAO_CASTRO);

        FuncionarioConsultaCompletaDto dto = consultaDeFuncionario.consultarCompletoPorCpf(CPF_PRE_CADASTRO, rh);

        assertThat(dto.getNome()).isEqualTo("Pré-cadastro Teste");
        assertThat(dto.getVinculos()).isEmpty();
    }

    @Test
    @DisplayName("Dado perfil só consulta, Quando colaborador não vinculado, Então bloqueia")
    void consultaSemPermissaoDeVinculoDeveExigirVinculo() {
        Funcionario preCadastro = Funcionario.builder().cpf(CPF_PRE_CADASTRO).nome("X").build();
        when(funcionarioRepository.findByCpf(CPF_PRE_CADASTRO)).thenReturn(preCadastro);
        when(instituicaoRepository.existsByCnpjAndFuncionarioCpf(INSTITUICAO_CASTRO, CPF_PRE_CADASTRO))
                .thenReturn(false);
        when(vinculoRepository.findByFuncionarioCpfAndInstituicaoId(CPF_PRE_CADASTRO, INSTITUICAO_CASTRO))
                .thenReturn(Optional.empty());

        UsuarioAutenticado professor = usuarioInstitucional(TipoFuncionario.PROFESSOR, INSTITUICAO_CASTRO);

        ExcecaoDeDominio ex = assertThrows(ExcecaoDeDominio.class,
                () -> consultaDeFuncionario.consultarCompletoPorCpf(CPF_PRE_CADASTRO, professor));

        assertThat(ex.getMessage()).contains("Funcionário não vinculado à instituição");
    }

    @Test
    @DisplayName("Dado perfil só consulta com colaborador vinculado na tabela, Quando consultar, Então retorna dados")
    void consultaRestritaAceitaVinculoSomenteNaTabelaDeVinculos() {
        Funcionario vinculado = Funcionario.builder().cpf("94325755004").nome("Colaborador").build();
        when(funcionarioRepository.findByCpf("94325755004")).thenReturn(vinculado);
        when(instituicaoRepository.existsByCnpjAndFuncionarioCpf(1L, "94325755004")).thenReturn(false);
        when(vinculoRepository.findByFuncionarioCpfAndInstituicaoId("94325755004", 1L))
                .thenReturn(Optional.of(new VinculoFuncionarioInstituicao()));
        when(vinculoRepository.findByFuncionarioCpfOrderByInstituicaoRazaoSocialAsc("94325755004"))
                .thenReturn(List.of());

        UsuarioAutenticado financeiro = usuarioInstitucional(TipoFuncionario.FINANCEIRO, 1L);

        FuncionarioConsultaCompletaDto dto = consultaDeFuncionario.consultarCompletoPorCpf("94325755004", financeiro);

        assertThat(dto.getNome()).isEqualTo("Colaborador");
    }

    private UsuarioAutenticado usuarioInstitucional(TipoFuncionario tipo, Long instituicaoId) {
        Funcionario f = Funcionario.builder()
                .cpf("11111111111")
                .tipoFuncionario(tipo)
                .build();
        return new UsuarioAutenticado(
                Usuario.builder().login("11111111111").role(UserRole.USER).build(),
                f,
                null,
                instituicaoId,
                SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                false,
                false);
    }
}

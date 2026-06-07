package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.InstituicaoDto;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GerenciadorDeInstituicao — consulta por ID")
class GerenciadorDeInstituicaoConsultaTest {

    @Mock
    private InstituicaoRepository instituicaoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GerenciadorDeInstituicao gerenciador;

    @Test
    @DisplayName("Dado instituição existente Quando consultar por ID Então retorna resumo sem funcionários")
    void deveRetornarResumoDaInstituicaoPorId() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        instituicao.setRazaoSocial("Judô Castro Team");
        instituicao.setCnpj("00000000000191");
        instituicao.setCadastroAtivo(true);

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(instituicao));

        InstituicaoDto dto = gerenciador.consultarInstituicaoId(1L);

        assertThat(dto.getRazaoSocial()).isEqualTo("Judô Castro Team");
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFuncionarios()).isEmpty();
    }

    @Test
    @DisplayName("Dado ID inexistente Quando consultar por ID Então lança exceção de não encontrado")
    void deveFalharQuandoInstituicaoNaoExiste() {
        when(instituicaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gerenciador.consultarInstituicaoId(99L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Instituição não encontrada");
    }
}

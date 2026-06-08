package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.PessoaListagemDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultaDeFuncionario — listagem resumida")
class ConsultaDeFuncionarioListagemTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private InstituicaoRepository instituicaoRepository;
    @Mock
    private VinculoFuncionarioInstituicaoRepository vinculoRepository;

    @InjectMocks
    private ConsultaDeFuncionario consultaDeFuncionario;

    @Test
    @DisplayName("Dado usuário institucional Quando listar Então retorna vínculos da instituição")
    void deveListarPorInstituicao() {
        Funcionario f = Funcionario.builder()
                .id(10L)
                .nome("Carlos")
                .cpf("94325755004")
                .dataDeNascimento(LocalDate.of(1990, 5, 20))
                .build();
        Instituicao i = new Instituicao();
        i.setId(1L);
        i.setRazaoSocial("Academia A");
        VinculoFuncionarioInstituicao v = new VinculoFuncionarioInstituicao();
        v.setId(100L);
        v.setFuncionario(f);
        v.setInstituicao(i);
        v.setTipoFuncionario(TipoFuncionario.ADMINISTRADOR);
        when(vinculoRepository.findByInstituicaoIdComDetalhes(1L)).thenReturn(List.of(v));

        UsuarioAutenticado usuario = new UsuarioAutenticado(
                Usuario.builder().login("94325755004").role(UserRole.USER).build(),
                f,
                null,
                1L,
                SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                false,
                false);

        List<PessoaListagemDto> lista = consultaDeFuncionario.listarParaListagem(usuario);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getNome()).isEqualTo("Carlos");
        assertThat(lista.get(0).getCargo()).isEqualTo("Administrador");
    }

    @Test
    @DisplayName("Dado master Quando listar Então duplica linhas por instituição")
    void deveListarMasterComInstituicoes() {
        Funcionario f = Funcionario.builder()
                .id(20L)
                .nome("Paulo")
                .cpf("15179950783")
                .dataDeNascimento(LocalDate.of(1985, 3, 10))
                .build();
        Instituicao i1 = new Instituicao();
        i1.setId(1L);
        i1.setRazaoSocial("Inst A");
        Instituicao i2 = new Instituicao();
        i2.setId(2L);
        i2.setRazaoSocial("Inst B");
        VinculoFuncionarioInstituicao v1 = vinculo(f, i1, TipoFuncionario.PROFESSOR);
        VinculoFuncionarioInstituicao v2 = vinculo(f, i2, TipoFuncionario.ADMINISTRADOR);
        when(vinculoRepository.findAllComDetalhes()).thenReturn(List.of(v1, v2));

        UsuarioAutenticado master = new UsuarioAutenticado(
                Usuario.builder().login("00000000191").role(UserRole.ADMIN).build(),
                Funcionario.builder().cpf("00000000191").build(),
                null,
                0L,
                SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.NAO_APLICAVEL,
                true,
                true);

        List<PessoaListagemDto> lista = consultaDeFuncionario.listarParaListagem(master);

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getInstituicaoNome()).isEqualTo("Inst A");
        assertThat(lista.get(0).getCargo()).isEqualTo("Professor");
        assertThat(lista.get(1).getInstituicaoNome()).isEqualTo("Inst B");
        assertThat(lista.get(1).getCargo()).isEqualTo("Administrador");
    }

    private VinculoFuncionarioInstituicao vinculo(Funcionario f, Instituicao i, TipoFuncionario tipo) {
        VinculoFuncionarioInstituicao v = new VinculoFuncionarioInstituicao();
        v.setFuncionario(f);
        v.setInstituicao(i);
        v.setTipoFuncionario(tipo);
        return v;
    }
}

package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.PermissaoSistema;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioAutenticadoPermissaoVinculoTest {

    @Test
    void deveUsarTipoDoVinculoInstituicaoParaPermissoes() {
        Usuario usuario = Usuario.builder().login("15179950783").password("x").role(UserRole.USER).build();
        Funcionario funcionario = Funcionario.builder()
                .cpf("15179950783")
                .tipoFuncionario(TipoFuncionario.PROFESSOR)
                .build();

        UsuarioAutenticado autenticado = new UsuarioAutenticado(
                usuario,
                funcionario,
                null,
                2L,
                SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                false,
                false,
                TipoFuncionario.ADMINISTRADOR,
                null);

        assertTrue(autenticado.getAuthorities().stream()
                .anyMatch(a -> PermissaoSistema.TURMA_GERENCIAR_ALUNOS.getCodigo().equals(a.getAuthority())));
        assertTrue(autenticado.getAuthorities().stream()
                .anyMatch(a -> PermissaoSistema.FUNCIONARIO_ATIVAR.getCodigo().equals(a.getAuthority())));
        assertTrue(autenticado.getAuthorities().stream()
                .anyMatch(a -> PermissaoSistema.TURMA_GERENCIAR.getCodigo().equals(a.getAuthority())));
    }
}

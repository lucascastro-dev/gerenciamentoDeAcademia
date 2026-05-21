package gerenciamentoDeAcademia.infra.seguranca;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioAutenticadoTest {

    @Test
    void diretorDeveReceberRoleMaster() {
        Usuario usuario = Usuario.builder().login("1").password("x").role(UserRole.ADMIN).build();
        Funcionario funcionario = Funcionario.builder()
                .tipoFuncionario(TipoFuncionario.DIRETOR)
                .cpf("1")
                .build();

        UsuarioAutenticado autenticado = new UsuarioAutenticado(usuario, funcionario);

        assertTrue(autenticado.getAuthorities().stream()
                .anyMatch(a -> "ROLE_MASTER".equals(a.getAuthority())));
    }
}

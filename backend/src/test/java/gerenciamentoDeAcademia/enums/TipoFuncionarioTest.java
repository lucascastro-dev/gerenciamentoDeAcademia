package gerenciamentoDeAcademia.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoFuncionarioTest {

    @Test
    void apenasDiretorDeveSerMaster() {
        assertTrue(TipoFuncionario.DIRETOR.isUsuarioMaster());
        assertFalse(TipoFuncionario.TI.isUsuarioMaster());
        assertFalse(TipoFuncionario.ADMINISTRADOR.isUsuarioMaster());
        assertEquals(PermissaoSistema.values().length, TipoFuncionario.DIRETOR.permissoesPadrao().size());
    }

    @Test
    void administradorDeveGerenciarTurmasSemSerMaster() {
        var permissoes = TipoFuncionario.ADMINISTRADOR.permissoesPadrao();
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR));
        assertFalse(TipoFuncionario.ADMINISTRADOR.isUsuarioMaster());
    }

    @Test
    void professorNaoDeveGerenciarTurmas() {
        var permissoes = TipoFuncionario.PROFESSOR.permissoesPadrao();
        assertTrue(permissoes.contains(PermissaoSistema.CERTIFICADO_GERAR));
        assertFalse(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR));
        assertFalse(permissoes.contains(PermissaoSistema.FINANCEIRO_VISUALIZAR));
    }

    @Test
    void terceirizadoRhTemEscopoLimitado() {
        var permissoes = TipoFuncionario.permissoesTerceirizado(AreaTerceirizado.RH);
        assertTrue(permissoes.contains(PermissaoSistema.FUNCIONARIO_CONSULTAR));
        assertFalse(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR));
    }

    @Test
    void deveResolverTipoPorDescricao() {
        assertEquals(TipoFuncionario.RECEPCIONISTA, TipoFuncionario.fromCargo("Recepcionista"));
        assertNotNull(TipoFuncionario.fromCargo("PROFESSOR"));
    }
}

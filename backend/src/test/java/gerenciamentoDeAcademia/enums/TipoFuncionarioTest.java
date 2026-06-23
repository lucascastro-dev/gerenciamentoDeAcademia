package gerenciamentoDeAcademia.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoFuncionarioTest {

    @Test
    void nenhumPerfilDeveSerMasterPorTipo() {
        assertFalse(TipoFuncionario.DIRETOR.isUsuarioMaster());
        assertFalse(TipoFuncionario.TI.isUsuarioMaster());
        assertFalse(TipoFuncionario.ADMINISTRADOR.isUsuarioMaster());
    }

    @Test
    void diretorPossuiPermissoesInstitucionaisEPedagogicas() {
        var permissoes = TipoFuncionario.DIRETOR.permissoesPadrao();
        assertFalse(permissoes.isEmpty());
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR));
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_PRESENCA));
        assertTrue(TipoFuncionario.DIRETOR.podeAtuarComoProfessor());
    }

    @Test
    void administradorGerenciaTurmasSemMenusPlataforma() {
        var permissoes = TipoFuncionario.ADMINISTRADOR.permissoesPadrao();
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR));
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR_ALUNOS));
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_PRESENCA));
        assertTrue(permissoes.contains(PermissaoSistema.PROGRAMACAO_GERENCIAR_ITENS));
        assertTrue(permissoes.contains(PermissaoSistema.CERTIFICADO_GERAR));
        assertFalse(permissoes.contains(PermissaoSistema.ACADEMIA_CONSULTAR));
        assertFalse(TipoFuncionario.ADMINISTRADOR.isUsuarioMaster());
        assertTrue(TipoFuncionario.ADMINISTRADOR.podeAtuarComoProfessor());
    }

    @Test
    void financeiroPossuiEscopoFinanceiroEConsultasInstitucionais() {
        var permissoes = TipoFuncionario.FINANCEIRO.permissoesPadrao();
        assertTrue(permissoes.contains(PermissaoSistema.DASHBOARD_VISUALIZAR));
        assertTrue(permissoes.contains(PermissaoSistema.FINANCEIRO_VISUALIZAR));
        assertTrue(permissoes.contains(PermissaoSistema.FINANCEIRO_COBRANCA));
        assertTrue(permissoes.contains(PermissaoSistema.FINANCEIRO_RELATORIO));
        assertTrue(permissoes.contains(PermissaoSistema.ALUNO_CONSULTAR));
        assertTrue(permissoes.contains(PermissaoSistema.FUNCIONARIO_CONSULTAR));
        assertTrue(permissoes.contains(PermissaoSistema.PLANO_INSTITUICAO_VISUALIZAR));
        assertFalse(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("Dado perfil PROFESSOR, Quando listar permissões, Então permite turmas alunos e presença sem gerenciar turma")
    void professorDeveTerPermissoesPedagogicasSemAdministrativas() {
        var permissoes = TipoFuncionario.PROFESSOR.permissoesPadrao();
        assertTrue(permissoes.contains(PermissaoSistema.CERTIFICADO_GERAR));
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR_ALUNOS));
        assertTrue(permissoes.contains(PermissaoSistema.TURMA_PRESENCA));
        assertTrue(permissoes.contains(PermissaoSistema.PROGRAMACAO_GERENCIAR_ITENS));
        assertFalse(permissoes.contains(PermissaoSistema.TURMA_GERENCIAR));
        assertFalse(permissoes.contains(PermissaoSistema.PROGRAMACAO_GERENCIAR));
        assertFalse(permissoes.contains(PermissaoSistema.FINANCEIRO_VISUALIZAR));
    }

    @Test
    void rhPossuiEscopoAdministrativoERecursosHumanos() {
        var permissoes = TipoFuncionario.RH.permissoesPadrao();
        assertTrue(permissoes.contains(PermissaoSistema.DASHBOARD_VISUALIZAR));
        assertTrue(permissoes.contains(PermissaoSistema.FUNCIONARIO_CONSULTAR));
        assertTrue(permissoes.contains(PermissaoSistema.FUNCIONARIO_ATIVAR));
        assertTrue(permissoes.contains(PermissaoSistema.RH_FOLHA_PONTO));
        assertTrue(permissoes.contains(PermissaoSistema.RH_FECHAMENTO_MENSAL));
        assertTrue(permissoes.contains(PermissaoSistema.RH_LANCAMENTO_HOLERITE));
        assertFalse(permissoes.contains(PermissaoSistema.ALUNO_CONSULTAR));
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

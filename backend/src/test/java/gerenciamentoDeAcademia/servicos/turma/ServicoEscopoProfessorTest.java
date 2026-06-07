package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeAcesso;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.instancio.Select.field;

@ExtendWith(SpringExtension.class)
class ServicoEscopoProfessorTest {

    private static final String CPF_PROFESSOR = "61482582007";

    @InjectMocks
    ServicoEscopoProfessor servicoEscopoProfessor;
    @Mock
    TurmaRepository turmaRepository;

    @Test
    @DisplayName("Dado turma do professor logado, Quando exigir escopo, Então retorna a turma")
    void deveRetornarTurmaQuandoPertenceAoProfessor() {
        Turma turma = turmaDoProfessor(CPF_PROFESSOR);
        Mockito.when(turmaRepository.findById(5L)).thenReturn(Optional.of(turma));

        Turma resultado = servicoEscopoProfessor.exigirTurmaDoProfessor(5L, usuarioProfessor(CPF_PROFESSOR));

        Assertions.assertEquals(5L, ReflectionTestUtils.getField(resultado, "id"));
    }

    @Test
    @DisplayName("Dado turma de outro professor, Quando exigir escopo, Então nega acesso")
    void deveNegarAcessoQuandoTurmaNaoPertenceAoProfessor() {
        Turma turma = turmaDoProfessor("99999999999");
        Mockito.when(turmaRepository.findById(5L)).thenReturn(Optional.of(turma));

        Assertions.assertThrows(
                ExcecaoDeAcesso.class,
                () -> servicoEscopoProfessor.exigirTurmaDoProfessor(5L, usuarioProfessor(CPF_PROFESSOR)));
    }

    @Test
    @DisplayName("Dado turma inexistente, Quando exigir escopo, Então retorna não encontrado")
    void deveRetornarNaoEncontradoQuandoTurmaInexistente() {
        Mockito.when(turmaRepository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                ExcecaoDeAcesso.class,
                () -> servicoEscopoProfessor.exigirTurmaDoProfessor(99L, usuarioProfessor(CPF_PROFESSOR)));
    }

    @Test
    @DisplayName("Dado operador master, Quando exigir escopo, Então ignora vínculo de professor")
    void devePermitirMasterSemVinculoDeProfessor() {
        Turma turma = turmaDoProfessor("99999999999");
        Mockito.when(turmaRepository.findById(5L)).thenReturn(Optional.of(turma));

        Turma resultado = servicoEscopoProfessor.exigirTurmaDoProfessor(5L, usuarioMaster());

        Assertions.assertNotNull(resultado);
    }

    private Turma turmaDoProfessor(String cpf) {
        Funcionario professor = Instancio.of(Funcionario.class)
                .set(field(Funcionario::getCpf), cpf)
                .create();
        Turma turma = new Turma();
        turma.setModalidade("Judô Baby");
        turma.setProfessor(professor);
        ReflectionTestUtils.setField(turma, "id", 5L);
        return turma;
    }

    private UsuarioAutenticado usuarioProfessor(String cpf) {
        Usuario usuario = Instancio.of(Usuario.class).set(field(Usuario::getLogin), cpf).create();
        return new UsuarioAutenticado(
                usuario,
                Instancio.of(Funcionario.class).create(),
                null,
                1L,
                SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.PAGAMENTO_CONFIRMADO,
                false,
                false);
    }

    private UsuarioAutenticado usuarioMaster() {
        return new UsuarioAutenticado(
                Instancio.of(Usuario.class).create(),
                Instancio.of(Funcionario.class).create(),
                null,
                0L,
                SituacaoCobranca.ATIVO,
                StatusFinanceiroInstituicao.NAO_APLICAVEL,
                true,
                true);
    }
}

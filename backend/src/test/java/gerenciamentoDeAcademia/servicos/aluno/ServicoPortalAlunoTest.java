package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.Usuario;
import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import gerenciamentoDeAcademia.enums.UserRole;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ServicoPortalAlunoTest {

    private static final String CPF = "83025278072";

    @InjectMocks
    ServicoPortalAluno servico;

    @Mock
    TurmaRepository turmaRepository;
    @Mock
    AlunoRepository alunoRepository;

    @Test
    @DisplayName("Dado aluno matriculado em turma da instituição, Quando listar turmas, Então retorna resumo")
    void deveListarTurmasDoAluno() {
        Aluno aluno = new Aluno();
        aluno.setCpf(CPF);
        aluno.setNome("Bernardo");

        Instituicao inst = new Instituicao();
        ReflectionTestUtils.setField(inst, "id", 3L);

        Turma turma = new Turma();
        turma.setModalidade("Judo Juvenil");
        turma.setInstituicao(inst);
        turma.setAlunos(new HashSet<>(List.of(aluno)));

        Funcionario prof = new Funcionario();
        prof.setNome("Lucas");
        turma.setProfessor(prof);

        Mockito.when(alunoRepository.findByCpf(CPF)).thenReturn(aluno);
        Mockito.when(turmaRepository.findTurmasDoAlunoNaInstituicao(CPF, 3L)).thenReturn(List.of(turma));

        var resultado = servico.listarMinhasTurmas(usuarioAluno(3L));

        Assertions.assertEquals(1, resultado.size());
        Assertions.assertEquals("Judo Juvenil", resultado.get(0).getModalidade());
        Assertions.assertEquals("Lucas", resultado.get(0).getProfessorNome());
    }

    private UsuarioAutenticado usuarioAluno(Long instituicaoId) {
        Usuario usuario = Usuario.builder().login(CPF).role(UserRole.ALUNO).build();
        Aluno aluno = new Aluno();
        aluno.setCpf(CPF);
        return new UsuarioAutenticado(
                usuario, null, aluno, instituicaoId,
                SituacaoCobranca.ATIVO, StatusFinanceiroInstituicao.NAO_APLICAVEL, false, false);
    }
}

package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class DesmatricularAlunoTest {

    @InjectMocks
    DesmatricularAluno desmatricularAluno;
    @Mock
    AlunoRepository alunoRepository;

    @Test
    void deveDesmatricularUmAluno() {
        String cpf = "123";
        Aluno aluno = Instancio.of(Aluno.class).set(field(Aluno::getCpf), cpf).create();
        Mockito.when(alunoRepository.findByCpf(cpf)).thenReturn(aluno);

        desmatricularAluno.excluirCadastro(cpf);

        Mockito.verify(alunoRepository).delete(any(Aluno.class));
    }

    @Test
    void deveRetornarMensagemDeCpfObrigatorioParaExcluirAluno() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> desmatricularAluno.excluirCadastro(""));

        Assertions.assertEquals("CPF é obrigatório para desmatricular o aluno!", mensagemDeErro.getMessage());
    }

    @Test
    void deveConsultarSeOAlunoExiste() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> desmatricularAluno.excluirCadastro("1345"));

        Assertions.assertEquals("Aluno não encontrado na base!", mensagemDeErro.getMessage());
    }
}

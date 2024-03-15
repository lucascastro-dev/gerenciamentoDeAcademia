package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
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
public class AlteraradorDeDadosDoAlunoTest {

    @InjectMocks
    AlteadorDeDadosDoAluno alteadorDeDadosDoAluno;
    @Mock
    AlunoRepository alunoRepository;

    @Test
    void deveAlterarOsDadosDeUmAluno() {
        Aluno alunoExistente = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "80430802080").create();
        AlunoDto alunoNovo = Instancio.of(AlunoDto.class)
                .set(field(AlunoDto::getCpf), "80430802080")
                .set(field(AlunoDto::getNome), "Aluno alterado").create();
        Mockito.when(alunoRepository.findByCpf(alunoNovo.getCpf())).thenReturn(alunoExistente);

        alteadorDeDadosDoAluno.alterarAluno(alunoNovo);

        Mockito.verify(alunoRepository).save(any(Aluno.class));
    }

    @Test
    void deveConsultarSeOAlunoExisteAntesDeAlterar() {
        AlunoDto alunoDto = Instancio.of(AlunoDto.class).set(field(AlunoDto::getCpf), "80430802080").create();
        Aluno alunoEncontrado = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "80430802080").create();
        Mockito.when(alunoRepository.findByCpf(alunoDto.getCpf())).thenReturn(alunoEncontrado);

        alteadorDeDadosDoAluno.alterarAluno(alunoDto);

        Mockito.verify(alunoRepository).findByCpf(alunoDto.getCpf());
    }

    @Test
    void deveRetornarMensagemDeErroSeNaoEncontrarAluno() {
        AlunoDto alunoDto = Instancio.of(AlunoDto.class).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteadorDeDadosDoAluno.alterarAluno(alunoDto));

        Assertions.assertEquals("Aluno não encontrado!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroSeTentarAlterarOCpf() {
        AlunoDto alunoParaAlterar = Instancio.of(AlunoDto.class).set(field(AlunoDto::getCpf), "123456").create();
        Aluno alunoEncontrado = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "123456789").create();
        Mockito.when(alunoRepository.findByCpf(alunoParaAlterar.getCpf())).thenReturn(alunoEncontrado);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> alteadorDeDadosDoAluno.alterarAluno(alunoParaAlterar));

        Assertions.assertEquals("Não é possível alterar o CPF do aluno!", mensagemDeErro.getMessage());
    }
}

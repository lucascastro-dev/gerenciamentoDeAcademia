package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
public class MontadorDeTurmaTeste {

    @InjectMocks
    MontadorDeTurma montadorDeTurma;
    @Mock
    TurmaRepository turmaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    FuncionarioRepository funcionarioRepository;

    @Test
    void deveMontarUmaTurmaComSucesso() {
        TurmaDto turmaDto = Instancio.of(TurmaDto.class).create();
        Aluno aluno = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "36305895023").create();
        Mockito.when(alunoRepository.findByCpf(anyString())).thenReturn(aluno);
        Funcionario funcionario = Instancio.of(Funcionario.class).set(field(Funcionario::getCpf), "36305895023").create();
        Mockito.when(funcionarioRepository.findByCpf(anyString())).thenReturn(funcionario);

        montadorDeTurma.montar(turmaDto);

        Mockito.verify(turmaRepository).save(Mockito.any(Turma.class));
    }

    @Test
    void deveRetornarMensagemDeErroQuandoFuncionarioPraCadastrarNaTurmaNaoForEncontradoNaBase() {
        TurmaDto turmaDto = Instancio.of(TurmaDto.class).create();
        Mockito.when(funcionarioRepository.findByCpf(anyString())).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> montadorDeTurma.montar(turmaDto));

        Assertions.assertEquals("Funcionario não encontrado", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroQuandoAlunoPraCadastrarNaTurmaNaoForEncontradoNaBase() {
        TurmaDto turmaDto = Instancio.of(TurmaDto.class).create();
        Funcionario funcionario = Instancio.of(Funcionario.class).set(field(Funcionario::getCpf), "36305895023").create();
        Mockito.when(funcionarioRepository.findByCpf(anyString())).thenReturn(funcionario);

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> montadorDeTurma.montar(turmaDto));

        Assertions.assertEquals("Aluno não encontrado", mensagemDeErro.getMessage());
    }
}

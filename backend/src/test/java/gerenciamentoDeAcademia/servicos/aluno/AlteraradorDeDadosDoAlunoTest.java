package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import org.instancio.Instancio;
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
    @Mock
    ServicoMatriculaInstituicao servicoMatriculaInstituicao;

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
    void deveAtualizarSomenteFinanceiroSemApagarDadosPessoais() {
        Aluno alunoExistente = Instancio.of(Aluno.class)
                .set(field(Aluno::getCpf), "80430802080")
                .set(field(Aluno::getNome), "Bernardo")
                .set(field(Aluno::getRg), "123456")
                .set(field(Aluno::getTelefone), "21999998888")
                .create();
        AlunoDto financeiro = new AlunoDto();
        financeiro.setCpf("80430802080");
        financeiro.setInstituicaoId(4L);
        financeiro.setValorMensalidade(200.0);
        financeiro.setDiaVencimentoMensalidade(15);

        Mockito.when(alunoRepository.findByCpf("80430802080")).thenReturn(alunoExistente);

        alteadorDeDadosDoAluno.alterarAluno(financeiro);

        org.junit.jupiter.api.Assertions.assertEquals("Bernardo", alunoExistente.getNome());
        org.junit.jupiter.api.Assertions.assertEquals("123456", alunoExistente.getRg());
        org.junit.jupiter.api.Assertions.assertEquals("21999998888", alunoExistente.getTelefone());
        Mockito.verify(servicoMatriculaInstituicao).atualizarFinanceiro(alunoExistente, financeiro);
    }

    @Test
    void deveConsultarSeOAlunoExisteAntesDeAlterar() {
        AlunoDto alunoDto = Instancio.of(AlunoDto.class).set(field(AlunoDto::getCpf), "80430802080").create();
        Aluno alunoEncontrado = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "80430802080").create();
        Mockito.when(alunoRepository.findByCpf(alunoDto.getCpf())).thenReturn(alunoEncontrado);

        alteadorDeDadosDoAluno.alterarAluno(alunoDto);

        Mockito.verify(alunoRepository).findByCpf(alunoDto.getCpf());
    }
}

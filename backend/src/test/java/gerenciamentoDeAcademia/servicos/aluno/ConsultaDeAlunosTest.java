package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
public class ConsultaDeAlunosTest {

    @InjectMocks
    ConsultaDeAlunos consultaDeAlunos;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    InstituicaoRepository instituicaoRepository;

    @Test
    void deveConsultarAlunosDaInstituicao() {
        consultaDeAlunos.listarAlunos(1L);

        Mockito.verify(alunoRepository).findDistinctByTurma_Instituicao_IdOrderByNomeAsc(1L);
    }

    @Test
    void deveConsultarUmAlunoPeloCpf() {
        Aluno alunoEncontrado = Instancio.of(Aluno.class).create();
        String cpf = "12345678909";
        Mockito.when(alunoRepository.findByCpf(cpf)).thenReturn(alunoEncontrado);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao(cpf, 1L)).thenReturn(true);

        consultaDeAlunos.consultaAlunoPorCpf(cpf, 1L);

        Mockito.verify(alunoRepository).findByCpf(cpf);
    }

    @Test
    void deveRetornarMensagemAlunoNaoEncontrado() {
        Mockito.when(alunoRepository.findByCpf("12345678909")).thenReturn(null);

        var mensagemDeErro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> consultaDeAlunos.consultaAlunoPorCpf("12345678909", 1L));

        Assertions.assertEquals("Aluno não encontrado na base.", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeCpfObrigatorioAoConsultarAlunoComCpfNull() {
        var mensagemDeErro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> consultaDeAlunos.consultaAlunoPorCpf(null, 1L));

        Assertions.assertEquals("CPF obrigatório com 11 dígitos para consulta do aluno.", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeCpfObrigatorioAoConsultarAlunoComCpfVazio() {
        var mensagemDeErro = Assertions.assertThrows(
                ExcecaoDeDominio.class,
                () -> consultaDeAlunos.consultaAlunoPorCpf("", 1L));

        Assertions.assertEquals("CPF obrigatório com 11 dígitos para consulta do aluno.", mensagemDeErro.getMessage());
    }
}

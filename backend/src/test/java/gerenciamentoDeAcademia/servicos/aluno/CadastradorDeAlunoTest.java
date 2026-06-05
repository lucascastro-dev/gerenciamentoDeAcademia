package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
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

import java.time.LocalDate;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@ExtendWith(SpringExtension.class)
public class CadastradorDeAlunoTest {

    private static final String CPF_VALIDO = "52998224725";

    @InjectMocks
    CadastradorDeAluno cadastradorDeAluno;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    ServicoAcessoAluno servicoAcessoAluno;
    @Mock
    ServicoVinculoAlunoInstituicao servicoVinculoAlunoInstituicao;
    @Mock
    InstituicaoRepository instituicaoRepository;
    @Mock
    ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    private AlunoDto dtoValido() {
        return Instancio.of(AlunoDto.class)
                .set(field(AlunoDto::getCpf), CPF_VALIDO)
                .set(field(AlunoDto::getInstituicaoId), 1L)
                .create();
    }

    @Test
    void deveCadastrarUmAluno() {
        AlunoDto alunoDto = dtoValido();
        Mockito.when(alunoRepository.findByCpf(CPF_VALIDO)).thenReturn(null);
        Mockito.when(alunoRepository.save(Mockito.any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));

        cadastradorDeAluno.cadastrar(alunoDto);

        Mockito.verify(alunoRepository).save(Mockito.any(Aluno.class));
        Mockito.verify(servicoVinculoAlunoInstituicao).vincularAlunoNaInstituicao(Mockito.eq(1L), Mockito.any(Aluno.class));
    }

    @Test
    void deveVincularAlunoExistenteSemCriarNovoRegistro() {
        AlunoDto alunoDto = Instancio.of(AlunoDto.class)
                .set(field(AlunoDto::getCpf), "52998224725")
                .set(field(AlunoDto::getInstituicaoId), 1L)
                .create();
        Aluno existente = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "52998224725").create();
        Mockito.when(alunoRepository.findByCpf("52998224725")).thenReturn(existente);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao("52998224725", 1L)).thenReturn(false);

        cadastradorDeAluno.cadastrar(alunoDto);

        Mockito.verify(alunoRepository, Mockito.never()).save(Mockito.any(Aluno.class));
        Mockito.verify(servicoVinculoAlunoInstituicao).vincularAlunoNaInstituicao(1L, existente);
    }

    @Test
    void deveImpedirMatriculaDuplicadaNaMesmaInstituicao() {
        AlunoDto alunoDto = Instancio.of(AlunoDto.class)
                .set(field(AlunoDto::getCpf), "52998224725")
                .set(field(AlunoDto::getInstituicaoId), 1L)
                .create();
        Aluno existente = Instancio.of(Aluno.class).set(field(Aluno::getCpf), "52998224725").create();
        Mockito.when(alunoRepository.findByCpf("52998224725")).thenReturn(existente);
        Mockito.when(instituicaoRepository.alunoVinculadoInstituicao("52998224725", 1L)).thenReturn(true);

        var ex = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(alunoDto));

        Assertions.assertEquals("Este CPF já está matriculado nesta instituição.", ex.getMessage());
    }

    @Test
    void deveRetornarMensagemDeAlunoObrigatorio() {
        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(null));

        Assertions.assertEquals("Obrigatório preencher dados do aluno", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeNomeDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setNome(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Nome é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeRgDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setRg(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("RG é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeCpfDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setCpf(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("CPF é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeEnderecoDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setEndereco(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Endereço é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeDataDeNascimentoDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setDataDeNascimento(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Data de nascimento é obrigatória!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeTelefoneDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setTelefone(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Telefone é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeValorDeMensalidadeDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setValorMensalidade(null);
        Mockito.when(alunoRepository.findByCpf(CPF_VALIDO)).thenReturn(null);
        Mockito.when(alunoRepository.save(Mockito.any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new ExcecaoDeDominio("Valor da mensalidade é obrigatório!"))
                .when(servicoMatriculaInstituicao).salvarFinanceiro(eq(1L), any(Aluno.class), any(AlunoDto.class));

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Valor da mensalidade é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeDiaVencimentoDaMensalidadeDeAlunoObrigatorio() {
        AlunoDto aluno = dtoValido();
        aluno.setDiaVencimentoMensalidade(null);
        Mockito.when(alunoRepository.findByCpf(CPF_VALIDO)).thenReturn(null);
        Mockito.when(alunoRepository.save(Mockito.any(Aluno.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new ExcecaoDeDominio("Dia de vencimento da mensalidade é obrigatório!"))
                .when(servicoMatriculaInstituicao).salvarFinanceiro(eq(1L), any(Aluno.class), any(AlunoDto.class));

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Dia de vencimento da mensalidade é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeNomeDoResponsavelObrigatorioQuandoAlunoForMenorDeIdade() {
        AlunoDto aluno = dtoValido();
        aluno.setDataDeNascimento(LocalDate.now());
        aluno.setNomeResponsavel(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Nome do responsável é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeTelefoneDoResponsavelObrigatorioQuandoAlunoForMenorDeIdade() {
        AlunoDto aluno = dtoValido();
        aluno.setDataDeNascimento(LocalDate.now());
        aluno.setTelefoneResponsavel(null);

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Telefone do responsável é obrigatório!", excecao.getMessage());
    }
}

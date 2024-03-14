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

import java.time.LocalDate;

import static org.instancio.Select.field;

@ExtendWith(SpringExtension.class)
public class CadastradorDeAlunoTest {

    @InjectMocks
    CadastradorDeAluno cadastradorDeAluno;
    @Mock
    AlunoRepository alunoRepository;

    @Test
    void deveCadastrarUmAluno() {
        AlunoDto alunoDto = Instancio.of(AlunoDto.class).create();

        cadastradorDeAluno.cadastrar(alunoDto);

        Mockito.verify(alunoRepository).save(Mockito.any(Aluno.class));
    }

    @Test
    void deveRetornarMensagemDeAlunoObrigatorio() {
        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(null));

        Assertions.assertEquals("Obrigatório preencher dados do aluno", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeNomeDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getNome), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Nome é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeRgDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getRg), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("RG é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeCpfDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getCpf), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("CPF é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeEnderecoDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getEndereco), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Endereço é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeDataDeNascimentoDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getDataDeNascimento), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Data de nascimento é obrigatória!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeTelefoneDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getTelefone), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Telefone é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeValorDeMensalidadeDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getValorMensalidade), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Valor da mensalidade é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeDiaVencimentoDaMensalidadeDeAlunoObrigatorio() {
        AlunoDto aluno = Instancio.of(AlunoDto.class).set(field(AlunoDto::getDiaVencimentoMensalidade), null).create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Dia de vencimento da mensalidade é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeNomeDoResponsavelObrigatorioQuandoAlunoForMenorDeIdade() {
        AlunoDto aluno = Instancio.of(AlunoDto.class)
                .set(field(AlunoDto::getDataDeNascimento), LocalDate.now())
                .set(field(AlunoDto::getNomeResponsavel), null)
                .create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Nome do responsável é obrigatório!", excecao.getMessage());
    }

    @Test
    void deveRetornarMensagemDeTelefoneDoResponsavelObrigatorioQuandoAlunoForMenorDeIdade() {
        AlunoDto aluno = Instancio.of(AlunoDto.class)
                .set(field(AlunoDto::getDataDeNascimento), LocalDate.now())
                .set(field(AlunoDto::getTelefoneResponsavel), null)
                .create();

        var excecao = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeAluno.cadastrar(aluno));

        Assertions.assertEquals("Telefone do responsável é obrigatório!", excecao.getMessage());
    }
}

package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
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
public class CadastradorDeFuncionarioTest {

    @InjectMocks
    CadastradorDeFuncionario cadastradorDeFuncionario;
    @Mock
    FuncionarioRepository funcionarioRepository;

    @Test
    void deveCadastrarUmFuncionario() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).create();

        cadastradorDeFuncionario.cadastrar(funcionarioDto);

        Mockito.verify(funcionarioRepository).save(any(Funcionario.class));
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioNull() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(null));

        Assertions.assertEquals("Obrigatório preencher dados do funcionario", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComNomeNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getNome), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Nome é obrigatório!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComRGNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getRg), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("RG é obrigatório!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComCpfNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getCpf), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("CPF é obrigatório!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComDataNascimentoNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getDataDeNascimento), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Data de nascimento é obrigatória!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComEnderecoNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getEndereco), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Endereço é obrigatório!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComTelefoneNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getTelefone), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Telefone é obrigatório!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComCargoNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getCargo), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Cargo é obrigatório!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComEspecializacaoNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getEspecializacao), null).create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Especialização é obrigatório!", mensagemDeErro.getMessage());
    }
}

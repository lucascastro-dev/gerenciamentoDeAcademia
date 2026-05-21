package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.auditoria.ServicoAuditoria;
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
    @Mock
    ServicoAuditoria servicoAuditoria;

    private FuncionarioDto dtoValido() {
        return Instancio.of(FuncionarioDto.class)
                .set(field(FuncionarioDto::getTipoFuncionario), TipoFuncionario.PROFESSOR)
                .set(field(FuncionarioDto::getEspecializacao), "Judô")
                .create();
    }

    @Test
    void deveCadastrarUmFuncionario() {
        FuncionarioDto funcionarioDto = dtoValido();
        Mockito.when(funcionarioRepository.save(any(Funcionario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        cadastradorDeFuncionario.cadastrar(funcionarioDto);

        Mockito.verify(funcionarioRepository).save(any(Funcionario.class));
        Mockito.verify(servicoAuditoria).registrar(Mockito.anyString(), Mockito.eq("FUNCIONARIO"),
                Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioNull() {
        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(null));

        Assertions.assertEquals("Obrigatório preencher dados do funcionario", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComNomeNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class).set(field(FuncionarioDto::getNome), null).create();
        funcionarioDto.setTipoFuncionario(TipoFuncionario.PROFESSOR);

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
    void deveRetornarMensagemDeErroAoCadastrarFuncionarioComTipoNulo() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class)
                .set(field(FuncionarioDto::getTipoFuncionario), null)
                .set(field(FuncionarioDto::getCargo), null)
                .create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Tipo de funcionário é obrigatório!", mensagemDeErro.getMessage());
    }

    @Test
    void deveRetornarMensagemDeErroAoCadastrarProfessorSemEspecializacao() {
        FuncionarioDto funcionarioDto = Instancio.of(FuncionarioDto.class)
                .set(field(FuncionarioDto::getTipoFuncionario), TipoFuncionario.PROFESSOR)
                .set(field(FuncionarioDto::getEspecializacao), null)
                .create();

        var mensagemDeErro = Assertions.assertThrows(ExcecaoDeDominio.class, () -> cadastradorDeFuncionario.cadastrar(funcionarioDto));

        Assertions.assertEquals("Especialização é obrigatória para professores!", mensagemDeErro.getMessage());
    }
}

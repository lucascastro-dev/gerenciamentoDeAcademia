package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.auditoria.ServicoAuditoria;
import gerenciamentoDeAcademia.servicos.interfaces.ICadastradorDeFuncionario;
import gerenciamentoDeAcademia.util.PoliticaSenha;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CadastradorDeFuncionario implements ICadastradorDeFuncionario {

    private final FuncionarioRepository funcionarioRepository;
    private final ServicoAuditoria servicoAuditoria;

    @Override
    public void cadastrar(FuncionarioDto funcionarioDto) {
        ExcecaoDeDominio.quandoNulo(funcionarioDto, "Obrigatório preencher dados do funcionario");
        if (funcionarioRepository.findByCpf(funcionarioDto.getCpf()) != null) {
            throw new ApplicationException("Funcionário já cadastrado!", HttpStatus.BAD_REQUEST);
        }

        Funcionario salvo = funcionarioRepository.save(new Funcionario(funcionarioDto));
        servicoAuditoria.registrar("CADASTRO", "FUNCIONARIO", salvo.getCpf(),
                "Novo funcionário: " + salvo.getNome() + " (" + salvo.getTipoFuncionario() + ")");
    }

    @Override
    public void cadastrarPreCadastro(FuncionarioDto funcionarioDto) {
        ExcecaoDeDominio.quandoNulo(funcionarioDto, "Obrigatório preencher dados");
        if (funcionarioRepository.findByCpf(funcionarioDto.getCpf()) != null) {
            throw new ApplicationException("CPF já cadastrado. Aguarde a ativação pelo RH ou faça login se já foi ativado.",
                    HttpStatus.BAD_REQUEST);
        }
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(funcionarioDto.getCpf(), "CPF é obrigatório!");
        PoliticaSenha.validarSenhaForte(funcionarioDto.getSenha());

        Funcionario salvo = funcionarioRepository.save(Funcionario.builder()
                .nome(funcionarioDto.getNome())
                .rg(funcionarioDto.getRg())
                .cpf(funcionarioDto.getCpf().replaceAll("\\D", ""))
                .dataDeNascimento(funcionarioDto.getDataDeNascimento())
                .endereco(funcionarioDto.getEndereco())
                .telefone(funcionarioDto.getTelefone())
                .senha(funcionarioDto.getSenha())
                .cadastroAtivo(false)
                .permitirGerenciarFuncoes(false)
                .build());
        servicoAuditoria.registrar("PRE_CADASTRO", "FUNCIONARIO", salvo.getCpf(),
                "Pré-cadastro aguardando ativação pelo RH: " + salvo.getNome());
    }

    @Override
    @Transactional
    public void atualizarMeuPerfil(Funcionario funcionario, FuncionarioDto funcionarioDto) {
        ExcecaoDeDominio.quandoNulo(funcionario, "Funcionário não encontrado");
        ExcecaoDeDominio.quandoNulo(funcionarioDto, "Dados obrigatórios");
        funcionario.atualizarDadosPessoais(funcionarioDto);
        funcionarioRepository.save(funcionario);
        servicoAuditoria.registrar("ALTERACAO", "FUNCIONARIO", funcionario.getCpf(),
                "Perfil atualizado pelo próprio usuário");
    }

    @Override
    @Transactional
    public void editar(FuncionarioDto funcionarioDto) {
        var funcionario = funcionarioRepository.findByCpf(funcionarioDto.getCpf());
        if (funcionario == null) {
            throw new ApplicationException("Funcionário não existe!", HttpStatus.BAD_REQUEST);
        }

        funcionario.atualizar(funcionarioDto);
        servicoAuditoria.registrar("ALTERACAO", "FUNCIONARIO", funcionario.getCpf(),
                "Dados atualizados para " + funcionario.getNome());
    }
}
package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.auditoria.ServicoAuditoria;
import gerenciamentoDeAcademia.servicos.interfaces.IExcluirCadastroPessoa;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExcluirFuncionario implements IExcluirCadastroPessoa {

    private final FuncionarioRepository funcionarioRepository;
    private final ServicoAuditoria servicoAuditoria;

    @Override
    public void excluirCadastro(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF é obrigatório para excluir funcionário da base!");

        var funcionarioParaExcluir = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionarioParaExcluir, "Funcionário não encontrado na base!");

        servicoAuditoria.registrar("EXCLUSAO", "FUNCIONARIO", funcionarioParaExcluir.getCpf(),
                "Exclusão do funcionário " + funcionarioParaExcluir.getNome());
        funcionarioRepository.delete(funcionarioParaExcluir);
    }
}

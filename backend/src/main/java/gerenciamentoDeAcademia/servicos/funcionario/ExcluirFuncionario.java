package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IExcluirCadastroPessoa;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Component
public class ExcluirFuncionario implements IExcluirCadastroPessoa {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Override
    public void excluirCadastro(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF é obrigatório para excluir funcionário da base!");

        var funcionarioParaExcluir = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionarioParaExcluir, "Funcionário não encontrado na base!");

        funcionarioRepository.delete(funcionarioParaExcluir);
    }
}

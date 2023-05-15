package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IExcluirCadastroPessoa;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
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
        if (cpf.isEmpty() || cpf == null)
            ExcecaoDeDominio.quandoTextoVazioOuNulo(cpf, "CPF é obrigatório para excluir funcionário da base!");

        var funcionarioParaExcluir = funcionarioRepository.findByCpf(cpf);

        if (funcionarioParaExcluir != null) {
            funcionarioRepository.delete(funcionarioParaExcluir);
        } else {
            ExcecaoDeDominio.quandoNulo(funcionarioParaExcluir.getCpf(), "Funcionário não encontrado na base!");
        }
    }
}

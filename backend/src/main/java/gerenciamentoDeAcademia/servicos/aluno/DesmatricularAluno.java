package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IExcluirCadastroPessoa;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DesmatricularAluno implements IExcluirCadastroPessoa {
    private final AlunoRepository alunoRepository;

    @Override
    public void excluirCadastro(String cpf) {
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF é obrigatório para desmatricular o aluno!");

        var alunoParaDesmatricular = alunoRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(alunoParaDesmatricular, "Aluno não encontrado na base!");

        alunoRepository.delete(alunoParaDesmatricular);
    }
}
package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IExcluirCadastroPessoa;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Component
public class DesmatricularAluno implements IExcluirCadastroPessoa {
    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public void excluirCadastro(String cpf) {
        if (cpf.isEmpty() || cpf == null)
            ExcecaoDeDominio.quandoTextoVazioOuNulo(cpf, "CPF é obrigatório para desmatricular o aluno!");

        var alunoParaDesmatricular = alunoRepository.findByCpf(cpf);

        if (alunoParaDesmatricular != null) {
            alunoRepository.delete(alunoParaDesmatricular);
        } else {
            ExcecaoDeDominio.quandoNulo(alunoParaDesmatricular.getCpf(), "Aluno não encontrado na base!");
        }
    }
}
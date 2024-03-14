package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.IExcluirCadastroPessoa;
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
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF é obrigatório para desmatricular o aluno!");

        var alunoParaDesmatricular = alunoRepository.findByCpf(cpf);

        if (alunoParaDesmatricular != null) {
            alunoRepository.delete(new Aluno(alunoParaDesmatricular));
        } else {
            ExcecaoDeDominio.quandoNulo(alunoParaDesmatricular, "Aluno não encontrado na base!");
        }
    }
}
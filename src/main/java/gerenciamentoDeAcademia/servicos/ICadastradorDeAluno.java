package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.AlunoCadastrado;

public interface ICadastradorDeAluno {
    AlunoCadastrado cadastrar(Aluno aluno);
}
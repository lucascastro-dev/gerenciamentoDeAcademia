package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;

public interface ICadastradorDeAluno {
    AlunoCadastrado cadastrar(Aluno aluno);
}
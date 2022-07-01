package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.FuncionarioCadastrado;

public interface ICadastradorDeFuncionario {
    FuncionarioCadastrado cadastrar(Funcionario funcionario);
}
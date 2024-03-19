package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.entidades.Funcionario;

import java.util.List;

public interface IConsultaDeFuncionario {
    List<Funcionario> listarFuncionarios();

    Funcionario consultarFuncionarioPorCpf(String cpf);
}

package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;

import java.util.List;

public interface IConsultaDeFuncionario {
    List<FuncionarioDto> listarFuncionarios();

    Funcionario consultarFuncionarioPorCpf(String cpf);
}

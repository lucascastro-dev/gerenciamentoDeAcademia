package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.FuncionarioDto;

import java.util.List;

public interface IConsultaDeFuncionario {
    List<FuncionarioDto> listarFuncionarios();

    FuncionarioDto consultarFuncionarioPorCpf(String cpf);
}

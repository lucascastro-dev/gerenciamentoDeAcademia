package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;

public interface ICadastradorDeFuncionario {
    Funcionario cadastrar(FuncionarioDto funcionarioDto);
}
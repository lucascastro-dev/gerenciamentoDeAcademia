package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.FuncionarioCadastrado;

public interface ICadastradorDeFuncionario {
    FuncionarioCadastrado cadastrar(FuncionarioDto funcionarioDto);
}
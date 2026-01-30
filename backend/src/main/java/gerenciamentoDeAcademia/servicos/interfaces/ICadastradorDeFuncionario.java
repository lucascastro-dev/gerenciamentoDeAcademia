package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.FuncionarioDto;

public interface ICadastradorDeFuncionario {
    void cadastrar(FuncionarioDto funcionarioDto);

    void editar(FuncionarioDto funcionarioDto);
}
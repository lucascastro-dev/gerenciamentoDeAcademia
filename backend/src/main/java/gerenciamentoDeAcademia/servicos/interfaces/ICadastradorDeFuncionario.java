package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.FuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;

public interface ICadastradorDeFuncionario {
    void cadastrar(FuncionarioDto funcionarioDto);

    void cadastrarPreCadastro(FuncionarioDto funcionarioDto);

    void editar(FuncionarioDto funcionarioDto);

    void atualizarMeuPerfil(Funcionario funcionario, FuncionarioDto funcionarioDto);
}
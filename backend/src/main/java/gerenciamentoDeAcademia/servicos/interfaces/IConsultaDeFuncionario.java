package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.FuncionarioConsultaCompletaDto;
import gerenciamentoDeAcademia.dto.PessoaListagemDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;

import java.util.List;

public interface IConsultaDeFuncionario {
    List<Funcionario> listarFuncionarios();

    List<PessoaListagemDto> listarParaListagem(UsuarioAutenticado usuario);

    Funcionario consultarFuncionarioPorCpf(String cpf);

    FuncionarioConsultaCompletaDto consultarCompletoPorCpf(String cpf, Long instituicaoId, boolean operadorPlataforma);
}

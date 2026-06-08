package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AlunoConsultaCompletaDto;
import gerenciamentoDeAcademia.dto.AlunoConsultaProfessorDto;
import gerenciamentoDeAcademia.dto.PessoaListagemDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;

import java.util.List;

public interface IConsultaDeAlunos {
    List<Aluno> listarAlunos(Long instituicaoId);

    List<PessoaListagemDto> listarParaListagem(UsuarioAutenticado usuario);

    Aluno consultaAlunoPorCpf(String cpf, Long instituicaoId);

    AlunoConsultaCompletaDto consultaCompletaPorCpf(String cpf, UsuarioAutenticado usuario);

    AlunoConsultaProfessorDto consultaProfessorPorCpf(String cpf, UsuarioAutenticado usuario);

    AlunoConsultaProfessorDto consultaProfessorPorId(Long alunoId, UsuarioAutenticado usuario);
}

package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;

import java.util.List;

public interface IConsultaDeAlunos {
    List<Aluno> listarAlunos(Long instituicaoId);

    Aluno consultaAlunoPorCpf(String cpf, Long instituicaoId);
}

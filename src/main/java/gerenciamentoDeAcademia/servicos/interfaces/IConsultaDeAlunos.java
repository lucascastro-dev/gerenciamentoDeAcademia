package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.entidades.Aluno;

import java.util.List;

public interface IConsultaDeAlunos {
    List<Aluno> listarAlunos();

    Aluno consultaAlunoPorCpf(String cpf);
}

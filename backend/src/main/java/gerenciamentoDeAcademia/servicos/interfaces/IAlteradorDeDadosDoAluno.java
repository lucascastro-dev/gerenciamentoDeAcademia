package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;

public interface IAlteradorDeDadosDoAluno {
    Aluno alterarAluno(AlunoDto alunoDto);
}

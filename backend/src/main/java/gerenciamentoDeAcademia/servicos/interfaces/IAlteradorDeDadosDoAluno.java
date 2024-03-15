package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;

public interface IAlteradorDeDadosDoAluno {
    void alterarAluno(AlunoDto alunoDto);
}

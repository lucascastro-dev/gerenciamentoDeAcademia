package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;

public interface ICadastradorDeAluno {
    Aluno cadastrar(AlunoDto alunoDto);
}
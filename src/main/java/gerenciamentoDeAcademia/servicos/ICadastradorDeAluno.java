package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.AlunoCadastrado;

public interface ICadastradorDeAluno {
    AlunoCadastrado cadastrar(AlunoDto alunoDto);
}
package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.Turma;

public interface IMontadorDeTurma {
    Turma montar(TurmaDto turmaDto);
}
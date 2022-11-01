package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.TurmaDto;
import gerenciamentoDeAcademia.entidades.TurmaMontada;

public interface IMontadorDeTurma {
    TurmaMontada montar(TurmaDto turmaDto);
}
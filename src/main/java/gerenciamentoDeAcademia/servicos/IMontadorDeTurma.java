package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.entidades.TurmaMontada;

public interface IMontadorDeTurma {
    TurmaMontada montar(Turma turma);
}
package gerenciamentoDeAcademia.servicos.interfaces;

import gerenciamentoDeAcademia.entidades.Turma;

import java.util.List;
import java.util.Optional;

public interface IConsultaDeTurma {
    List<Turma> listarTurmas();
    Optional<Turma> buscarTurmaPorId(Long id);
    List<Turma> buscarTurmaPorModalidade(String modalidade);
}

package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.PresencaAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PresencaAlunoRepository extends JpaRepository<PresencaAluno, Long> {
    List<PresencaAluno> findByTurmaIdAndDataAulaBetween(Long turmaId, LocalDate inicio, LocalDate fim);
}

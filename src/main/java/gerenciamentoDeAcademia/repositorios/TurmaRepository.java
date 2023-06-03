package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findAll();

    Turma findById(String id);

    List<Turma> findByModalidade(String modalidade);
}

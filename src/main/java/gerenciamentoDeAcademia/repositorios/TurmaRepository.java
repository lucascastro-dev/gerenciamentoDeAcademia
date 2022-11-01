package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.TurmaMontada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurmaRepository extends JpaRepository<TurmaMontada, Long> {
}

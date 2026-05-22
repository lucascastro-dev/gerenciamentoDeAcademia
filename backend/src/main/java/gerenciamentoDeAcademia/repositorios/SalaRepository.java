package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {

    List<Sala> findByInstituicao_IdAndAtivaTrueOrderByNomeAsc(Long instituicaoId);

    List<Sala> findByInstituicao_IdOrderByNomeAsc(Long instituicaoId);
}

package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.ConferenciaPontoMensal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConferenciaPontoMensalRepository extends JpaRepository<ConferenciaPontoMensal, Long> {

    Optional<ConferenciaPontoMensal> findByInstituicao_IdAndMesCompetenciaAndAnoCompetencia(
            Long instituicaoId, Integer mesCompetencia, Integer anoCompetencia);
}

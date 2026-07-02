package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.CobrancaExterna;
import gerenciamentoDeAcademia.enums.StatusCobrancaExterna;
import gerenciamentoDeAcademia.enums.TipoCobrancaExterna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CobrancaExternaRepository extends JpaRepository<CobrancaExterna, Long> {

    Optional<CobrancaExterna> findByIdAndInstituicao_Id(Long id, Long instituicaoId);

    Optional<CobrancaExterna> findByIdExterno(String idExterno);

    List<CobrancaExterna> findByInstituicao_IdAndCpfAlunoAndTipoAndStatusOrderByCriadoEmDesc(
            Long instituicaoId,
            String cpfAluno,
            TipoCobrancaExterna tipo,
            StatusCobrancaExterna status);

    List<CobrancaExterna> findByInstituicao_IdAndCpfAlunoAndTipoAndAnoCompetenciaOrderByMesCompetenciaAsc(
            Long instituicaoId,
            String cpfAluno,
            TipoCobrancaExterna tipo,
            Integer anoCompetencia);

    List<CobrancaExterna> findByInstituicao_IdAndTipoAndMesCompetenciaAndAnoCompetencia(
            Long instituicaoId,
            TipoCobrancaExterna tipo,
            Integer mesCompetencia,
            Integer anoCompetencia);
}

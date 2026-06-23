package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.DocumentoRemuneracaoColaborador;
import gerenciamentoDeAcademia.enums.TipoDocumentoRemuneracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRemuneracaoColaboradorRepository extends JpaRepository<DocumentoRemuneracaoColaborador, Long> {

    List<DocumentoRemuneracaoColaborador> findByCpfColaboradorAndInstituicao_IdAndMesCompetenciaAndAnoCompetenciaOrderByTipoAsc(
            String cpfColaborador, Long instituicaoId, Integer mes, Integer ano);

    Optional<DocumentoRemuneracaoColaborador> findByInstituicao_IdAndCpfColaboradorAndTipoAndMesCompetenciaAndAnoCompetencia(
            Long instituicaoId,
            String cpfColaborador,
            TipoDocumentoRemuneracao tipo,
            Integer mes,
            Integer ano);

    Optional<DocumentoRemuneracaoColaborador> findByIdAndCpfColaboradorAndInstituicao_Id(
            Long id, String cpfColaborador, Long instituicaoId);

    List<DocumentoRemuneracaoColaborador> findByInstituicao_IdAndMesCompetenciaAndAnoCompetenciaAndTipo(
            Long instituicaoId, Integer mes, Integer ano, TipoDocumentoRemuneracao tipo);
}

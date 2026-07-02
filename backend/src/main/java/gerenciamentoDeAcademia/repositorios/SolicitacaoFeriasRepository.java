package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.SolicitacaoFerias;
import gerenciamentoDeAcademia.enums.StatusSolicitacaoFerias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SolicitacaoFeriasRepository extends JpaRepository<SolicitacaoFerias, Long> {

    List<SolicitacaoFerias> findByInstituicao_IdAndCpfColaboradorOrderByCriadoEmDesc(
            Long instituicaoId, String cpfColaborador);

    List<SolicitacaoFerias> findByInstituicao_IdOrderByCriadoEmDesc(Long instituicaoId);

    List<SolicitacaoFerias> findByInstituicao_IdAndStatusOrderByCriadoEmDesc(
            Long instituicaoId, StatusSolicitacaoFerias status);

    List<SolicitacaoFerias> findByInstituicao_IdAndCpfColaboradorAndInicioPeriodoAquisitivo(
            Long instituicaoId, String cpfColaborador, LocalDate inicioPeriodoAquisitivo);
}

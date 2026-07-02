package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.SolicitacaoAjustePonto;
import gerenciamentoDeAcademia.enums.StatusSolicitacaoAjustePonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoAjustePontoRepository extends JpaRepository<SolicitacaoAjustePonto, Long> {

    List<SolicitacaoAjustePonto> findByInstituicao_IdAndCpfColaboradorOrderByCriadoEmDesc(
            Long instituicaoId, String cpfColaborador);

    List<SolicitacaoAjustePonto> findByInstituicao_IdAndStatusOrderByCriadoEmAsc(
            Long instituicaoId, StatusSolicitacaoAjustePonto status);

    List<SolicitacaoAjustePonto> findByInstituicao_IdOrderByCriadoEmDesc(Long instituicaoId);
}

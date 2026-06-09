package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.auditoria.RevisaoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisaoAuditoriaRepository extends JpaRepository<RevisaoAuditoria, Long> {
}

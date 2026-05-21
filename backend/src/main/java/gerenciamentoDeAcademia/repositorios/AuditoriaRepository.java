package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
}

package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    List<RegistroAuditoria> findAllByOrderByDataHoraDesc();
}

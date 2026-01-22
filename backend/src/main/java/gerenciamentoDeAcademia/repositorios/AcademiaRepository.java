package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Academia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademiaRepository extends JpaRepository<Academia, Long> {
    Academia findByCnpj(String cnpjAcademia);
}

package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.AlunoCadastrado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<AlunoCadastrado, Long> {
}
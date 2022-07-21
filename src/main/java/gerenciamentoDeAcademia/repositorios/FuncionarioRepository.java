package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.FuncionarioCadastrado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<FuncionarioCadastrado, Long> {
}
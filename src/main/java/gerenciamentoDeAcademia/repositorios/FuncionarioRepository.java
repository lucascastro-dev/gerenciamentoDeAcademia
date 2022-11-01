package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.FuncionarioCadastrado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionarioRepository extends JpaRepository<FuncionarioCadastrado, Long> {
}
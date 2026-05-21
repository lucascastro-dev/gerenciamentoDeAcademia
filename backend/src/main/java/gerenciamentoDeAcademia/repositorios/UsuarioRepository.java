package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByLogin(String login);
    Usuario findByLogin(String login);
}

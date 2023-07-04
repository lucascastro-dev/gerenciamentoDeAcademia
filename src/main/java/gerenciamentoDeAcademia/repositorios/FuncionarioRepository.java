package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionarioRepository extends
        JpaRepository<Funcionario, Long>,
        RevisionRepository<Funcionario, Long, Long> {
    List<Funcionario> findAll();

    Funcionario findByCpf(String cpf);

}
package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findFirstByCpfOrderByIdAsc(String cpf);

    default Aluno findByCpf(String cpf) {
        return findFirstByCpfOrderByIdAsc(cpf).orElse(null);
    }

    boolean existsByCpf(String cpf);

    List<Aluno> findDistinctByTurma_Instituicao_IdOrderByNomeAsc(Long instituicaoId);

    List<Aluno> findAllByOrderByNomeAsc();
}
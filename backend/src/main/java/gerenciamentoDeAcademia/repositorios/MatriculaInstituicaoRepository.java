package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatriculaInstituicaoRepository extends JpaRepository<MatriculaInstituicao, Long> {

    Optional<MatriculaInstituicao> findByAluno_CpfAndInstituicao_Id(String cpf, Long instituicaoId);

    Optional<MatriculaInstituicao> findByAluno_IdAndInstituicao_Id(Long alunoId, Long instituicaoId);

    List<MatriculaInstituicao> findByInstituicao_IdOrderByAluno_NomeAsc(Long instituicaoId);

    List<MatriculaInstituicao> findByAluno_Cpf(String cpf);
}

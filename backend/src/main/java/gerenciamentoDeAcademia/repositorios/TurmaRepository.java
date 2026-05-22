package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findByModalidade(String modalidade);

    List<Turma> findByProfessor_Cpf(String cpfProfessor);

    List<Turma> findByAlunos_CpfAndInstituicao_Id(String cpfAluno, Long instituicaoId);

    List<Turma> findByInstituicao_Id(Long instituicaoId);

    java.util.Optional<Turma> findFirstByInstituicao_IdAndModalidadeOrderByIdAsc(Long instituicaoId, String modalidade);
}

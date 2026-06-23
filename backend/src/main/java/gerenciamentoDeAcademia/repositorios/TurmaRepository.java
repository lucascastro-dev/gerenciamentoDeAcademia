package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findByModalidade(String modalidade);

    List<Turma> findByProfessor_Cpf(String cpfProfessor);

    List<Turma> findByAlunos_CpfAndInstituicao_Id(String cpfAluno, Long instituicaoId);

    List<Turma> findByInstituicao_Id(Long instituicaoId);

    java.util.Optional<Turma> findFirstByInstituicao_IdAndModalidadeOrderByIdAsc(Long instituicaoId, String modalidade);

    /**
     * Sem ORDER BY no SQL: PostgreSQL exige que colunas do ORDER BY apareçam no SELECT com DISTINCT.
     * Ordenação feita em {@link gerenciamentoDeAcademia.servicos.aluno.ConsultaDeAlunos}.
     */
    @Query("SELECT DISTINCT t FROM Turma t JOIN FETCH t.instituicao JOIN t.alunos a WHERE a.cpf = :cpf")
    List<Turma> findTurmasMatriculadasPorCpf(@Param("cpf") String cpf);

    @Query("SELECT DISTINCT t FROM Turma t LEFT JOIN FETCH t.instituicao LEFT JOIN FETCH t.professor")
    List<Turma> findAllComInstituicaoEProfessor();

    @Query("""
            SELECT DISTINCT t FROM Turma t
            LEFT JOIN FETCH t.instituicao
            LEFT JOIN FETCH t.professor
            WHERE t.instituicao.id = :instituicaoId
            """)
    List<Turma> findByInstituicao_IdComDetalhes(@Param("instituicaoId") Long instituicaoId);

    @Query("""
            SELECT DISTINCT t FROM Turma t
            LEFT JOIN FETCH t.dias
            LEFT JOIN FETCH t.professor
            WHERE t.instituicao.id = :instituicaoId
            """)
    List<Turma> findByInstituicao_IdComDias(@Param("instituicaoId") Long instituicaoId);

    @Query("""
            SELECT t FROM Turma t
            LEFT JOIN FETCH t.alunos
            WHERE t.id = :id
            """)
    java.util.Optional<Turma> findByIdComAlunos(@Param("id") Long id);
}

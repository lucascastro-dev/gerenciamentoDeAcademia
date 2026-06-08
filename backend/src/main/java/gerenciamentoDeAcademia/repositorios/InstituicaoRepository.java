package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.enums.StatusFinanceiroInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    Instituicao findByCnpj(String cnpjInstituicao);

    @Query("SELECT COUNT(a) > 0 FROM Instituicao a JOIN a.funcionarios f WHERE a.id = :vinculo AND f.cpf = :cpf")
    boolean existsByCnpjAndFuncionarioCpf(@Param("vinculo") Long vinculo, @Param("cpf") String cpf);

    @Query("""
            SELECT DISTINCT a FROM Instituicao a JOIN a.funcionarios f
            WHERE f.cpf = :cpf AND a.cadastroAtivo = true
            ORDER BY a.razaoSocial
            """)
    List<Instituicao> findInstituicoesPorCpfFuncionario(@Param("cpf") String cpf);

    long countByCadastroAtivoTrue();

    @Query("SELECT COUNT(i) FROM Instituicao i WHERE i.cadastroAtivo = false OR i.cadastroAtivo IS NULL")
    long countCadastroInativo();

    long countByStatusFinanceiro(StatusFinanceiroInstituicao statusFinanceiro);

    @Query("""
            SELECT DISTINCT f FROM gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao v
            JOIN v.funcionario f
            WHERE v.instituicao.id = :instituicaoId
            AND v.tipoFuncionario = gerenciamentoDeAcademia.enums.TipoFuncionario.PROFESSOR
            AND f.cadastroAtivo = true
            ORDER BY f.nome
            """)
    List<gerenciamentoDeAcademia.entidades.Funcionario> findProfessoresAtivosPorInstituicao(@Param("instituicaoId") Long instituicaoId);

    @Query("SELECT DISTINCT f FROM Instituicao i JOIN i.funcionarios f WHERE i.id = :instituicaoId ORDER BY f.nome")
    List<gerenciamentoDeAcademia.entidades.Funcionario> findFuncionariosPorInstituicao(@Param("instituicaoId") Long instituicaoId);

    @Query("""
            SELECT DISTINCT a FROM Instituicao a
            JOIN Turma t ON t.instituicao = a
            JOIN t.alunos al
            WHERE al.cpf = :cpf AND a.cadastroAtivo = true
            ORDER BY a.razaoSocial
            """)
    List<Instituicao> findInstituicoesPorCpfAluno(@Param("cpf") String cpf);

    @Query("""
            SELECT CASE WHEN COUNT(al) > 0 THEN true ELSE false END
            FROM Aluno al
            WHERE al.cpf = :cpf
            AND (
                EXISTS (SELECT 1 FROM Turma t JOIN t.alunos a WHERE a = al AND t.instituicao.id = :instituicaoId)
                OR EXISTS (SELECT 1 FROM MatriculaInstituicao m WHERE m.aluno = al AND m.instituicao.id = :instituicaoId)
            )
            """)
    boolean alunoVinculadoInstituicao(@Param("cpf") String cpf, @Param("instituicaoId") Long instituicaoId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("UPDATE Instituicao i SET i.cadastroAtivo = false WHERE i.cadastroAtivo IS NULL")
    int normalizarCadastroAtivoNulo();
}

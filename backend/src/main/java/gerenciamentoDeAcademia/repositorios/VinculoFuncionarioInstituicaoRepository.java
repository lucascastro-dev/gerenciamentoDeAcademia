package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VinculoFuncionarioInstituicaoRepository extends JpaRepository<VinculoFuncionarioInstituicao, Long> {

    boolean existsByFuncionarioIdAndInstituicaoId(Long funcionarioId, Long instituicaoId);

    Optional<VinculoFuncionarioInstituicao> findByFuncionarioCpfAndInstituicaoId(String cpf, Long instituicaoId);

    List<VinculoFuncionarioInstituicao> findByFuncionarioCpfOrderByInstituicaoRazaoSocialAsc(String cpf);

    @Query("""
            SELECT v FROM VinculoFuncionarioInstituicao v
            JOIN FETCH v.funcionario f
            JOIN FETCH v.instituicao i
            WHERE i.id = :instituicaoId
            ORDER BY f.nome ASC
            """)
    List<VinculoFuncionarioInstituicao> findByInstituicaoIdComDetalhes(@Param("instituicaoId") Long instituicaoId);

    @Query("""
            SELECT v FROM VinculoFuncionarioInstituicao v
            JOIN FETCH v.funcionario f
            JOIN FETCH v.instituicao i
            ORDER BY f.nome ASC, i.razaoSocial ASC
            """)
    List<VinculoFuncionarioInstituicao> findAllComDetalhes();
}

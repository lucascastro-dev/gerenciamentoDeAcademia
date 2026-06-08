package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssinaturaPlataformaRepository extends JpaRepository<AssinaturaPlataforma, Long> {
    Optional<AssinaturaPlataforma> findByInstituicao_Id(Long instituicaoId);
    boolean existsByInstituicao_Id(Long instituicaoId);

    @Query("""
            SELECT a FROM AssinaturaPlataforma a
            WHERE a.ativo = true AND a.dataFim < CURRENT_DATE
            """)
    List<AssinaturaPlataforma> findAssinaturasVencidas();

    @Query("SELECT a FROM AssinaturaPlataforma a JOIN FETCH a.instituicao")
    List<AssinaturaPlataforma> findAllComInstituicao();
}

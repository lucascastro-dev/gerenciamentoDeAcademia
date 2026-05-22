package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.AssinaturaPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssinaturaPlataformaRepository extends JpaRepository<AssinaturaPlataforma, Long> {
    Optional<AssinaturaPlataforma> findByInstituicao_Id(Long instituicaoId);
    boolean existsByInstituicao_Id(Long instituicaoId);
}

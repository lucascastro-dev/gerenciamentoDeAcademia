package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Academia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademiaRepository extends JpaRepository<Academia, Long> {
    Academia findByCnpj(String cnpjAcademia);

    @Query("SELECT COUNT(a) > 0 FROM Academia a JOIN a.funcionarios f WHERE a.id = :vinculo AND f.cpf = :cpf")
    boolean existsByCnpjAndFuncionarioCpf(@Param("vinculo") Long vinculo, @Param("cpf") String cpf);
}

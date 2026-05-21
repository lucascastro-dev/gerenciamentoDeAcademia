package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.Academia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademiaRepository extends JpaRepository<Academia, Long> {
    Academia findByCnpj(String cnpjAcademia);

    @Query("SELECT COUNT(a) > 0 FROM Academia a JOIN a.funcionarios f WHERE a.id = :vinculo AND f.cpf = :cpf")
    boolean existsByCnpjAndFuncionarioCpf(@Param("vinculo") Long vinculo, @Param("cpf") String cpf);

    @Query("SELECT DISTINCT a FROM Academia a JOIN a.funcionarios f WHERE f.cpf = :cpf ORDER BY a.razaoSocial")
    List<Academia> findInstituicoesPorCpfFuncionario(@Param("cpf") String cpf);

    @Query("SELECT DISTINCT f FROM Academia a JOIN a.funcionarios f WHERE a.id = :academiaId AND f.tipoFuncionario = gerenciamentoDeAcademia.enums.TipoFuncionario.PROFESSOR AND f.cadastroAtivo = true ORDER BY f.nome")
    List<gerenciamentoDeAcademia.entidades.Funcionario> findProfessoresAtivosPorAcademia(@Param("academiaId") Long academiaId);
}

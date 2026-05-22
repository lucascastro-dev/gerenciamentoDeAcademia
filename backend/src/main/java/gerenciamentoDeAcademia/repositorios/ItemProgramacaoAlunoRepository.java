package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemProgramacaoAlunoRepository extends JpaRepository<ItemProgramacaoAluno, Long> {

    List<ItemProgramacaoAluno> findByAluno_CpfAndInstituicao_IdOrderByDataPrevistaAsc(String cpf, Long instituicaoId);

    List<ItemProgramacaoAluno> findByInstituicao_IdOrderByDataPrevistaAscIdAsc(Long instituicaoId);
}

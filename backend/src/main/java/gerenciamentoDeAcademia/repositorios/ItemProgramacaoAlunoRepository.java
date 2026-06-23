package gerenciamentoDeAcademia.repositorios;

import gerenciamentoDeAcademia.entidades.ItemProgramacaoAluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemProgramacaoAlunoRepository extends JpaRepository<ItemProgramacaoAluno, Long> {

    List<ItemProgramacaoAluno> findByAluno_CpfAndInstituicao_IdOrderByDataPrevistaAsc(String cpf, Long instituicaoId);

    List<ItemProgramacaoAluno> findByInstituicao_IdOrderByDataPrevistaAscIdAsc(Long instituicaoId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT DISTINCT i FROM ItemProgramacaoAluno i
            LEFT JOIN i.turma t LEFT JOIN t.alunos ta
            WHERE i.instituicao.id = :instituicaoId
            AND (i.aluno.cpf = :cpf OR ta.cpf = :cpf)
            ORDER BY i.dataPrevista ASC, i.id ASC
            """)
    List<ItemProgramacaoAluno> findVisiveisParaAluno(
            @org.springframework.data.repository.query.Param("cpf") String cpf,
            @org.springframework.data.repository.query.Param("instituicaoId") Long instituicaoId);
}

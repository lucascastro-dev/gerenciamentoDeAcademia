package gerenciamentoDeAcademia.servicos.instituicao;

import gerenciamentoDeAcademia.dto.DashboardResumoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.MatriculaInstituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.MatriculaInstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoVinculoAlunoInstituicao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServicoDashboardInstituicao {

    private final MatriculaInstituicaoRepository matriculaInstituicaoRepository;
    private final AlunoRepository alunoRepository;
    private final VinculoFuncionarioInstituicaoRepository vinculoFuncionarioInstituicaoRepository;
    private final TurmaRepository turmaRepository;

    @Transactional(readOnly = true)
    public DashboardResumoDto resumo(Long instituicaoId) {
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição não identificada na sessão.");

        long totalAlunos = alunosVinculadosInstituicao(instituicaoId).size();
        long funcionariosAtivos = vinculoFuncionarioInstituicaoRepository.findByInstituicaoIdComDetalhes(instituicaoId).stream()
                .filter(v -> v.getFuncionario() != null && Boolean.TRUE.equals(v.getFuncionario().getCadastroAtivo()))
                .count();
        long totalTurmas = turmaRepository.findByInstituicao_IdComDetalhes(instituicaoId).stream()
                .filter(t -> !ServicoVinculoAlunoInstituicao.MODALIDADE_MATRICULA.equals(t.getModalidade()))
                .count();

        return new DashboardResumoDto(totalAlunos, funcionariosAtivos, totalTurmas);
    }

    private Map<Long, Aluno> alunosVinculadosInstituicao(Long instituicaoId) {
        Map<Long, Aluno> porId = new LinkedHashMap<>();

        matriculaInstituicaoRepository.findByInstituicao_IdOrderByAluno_NomeAsc(instituicaoId).stream()
                .map(MatriculaInstituicao::getAluno)
                .filter(a -> a != null && a.getId() != null)
                .forEach(a -> porId.putIfAbsent(a.getId(), a));

        alunoRepository.findDistinctByTurma_Instituicao_IdOrderByNomeAsc(instituicaoId).stream()
                .filter(a -> a != null && a.getId() != null)
                .forEach(a -> porId.putIfAbsent(a.getId(), a));

        return porId;
    }
}

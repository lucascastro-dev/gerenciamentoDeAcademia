package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.servicos.aluno.ServicoVinculoAlunoInstituicao;
import gerenciamentoDeAcademia.servicos.interfaces.IConsultaDeTurma;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsultaDeTurma implements IConsultaDeTurma {
    private final TurmaRepository turmaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Turma> listarTurmas() {
        List<Turma> turmas = turmaRepository.findAllComInstituicaoEProfessor();
        materializarColecoes(turmas);
        return turmas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turma> listarTurmas(
            UsuarioAutenticado usuario,
            Long instituicaoIdFiltro,
            String professorCpf,
            List<String> dias) {
        Long escopoInstituicao = resolverEscopoInstituicao(usuario, instituicaoIdFiltro);
        String cpfProfessor = normalizarCpf(professorCpf);
        Set<String> diasFiltro = dias != null
                ? dias.stream().filter(d -> d != null && !d.isBlank()).collect(Collectors.toSet())
                : Set.of();

        List<Turma> base = escopoInstituicao != null
                ? turmaRepository.findByInstituicao_IdComDetalhes(escopoInstituicao)
                : turmaRepository.findAllComInstituicaoEProfessor();

        materializarColecoes(base);

        return base.stream()
                .filter(t -> !ServicoVinculoAlunoInstituicao.MODALIDADE_MATRICULA.equals(t.getModalidade()))
                .filter(t -> cpfProfessor == null
                        || (t.getProfessor() != null && cpfProfessor.equals(t.getProfessor().getCpf())))
                .filter(t -> diasFiltro.isEmpty()
                        || (t.getDias() != null && t.getDias().stream().anyMatch(diasFiltro::contains)))
                .sorted(Comparator.comparing(Turma::getModalidade, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private void materializarColecoes(List<Turma> turmas) {
        turmas.forEach(t -> {
            if (t.getDias() != null) {
                t.getDias().size();
            }
        });
    }

    private Long resolverEscopoInstituicao(UsuarioAutenticado usuario, Long instituicaoIdFiltro) {
        boolean master = usuario != null && usuario.isOperadorPlataforma();
        if (master) {
            return instituicaoIdFiltro;
        }
        Long sessao = usuario != null ? usuario.getInstituicaoId() : null;
        ExcecaoDeDominio.quandoNulo(sessao, "Instituição não identificada na sessão.");
        return sessao;
    }

    private String normalizarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }
        String limpo = cpf.replaceAll("\\D", "");
        return limpo.isEmpty() ? null : limpo;
    }

    @Override
    public Optional<Turma> buscarTurmaPorId(Long id) {
        ExcecaoDeDominio.quandoNulo(id, "ID obrigatório para consulta da turma");

        return turmaRepository.findById(id);
    }

    @Override
    public List<Turma> buscarTurmaPorModalidade(String modalidade) {
        ExcecaoDeDominio.quandoNuloOuVazio(modalidade, "Modalidade obrigatória para consulta da turma");

        return turmaRepository.findByModalidade(modalidade);
    }
}

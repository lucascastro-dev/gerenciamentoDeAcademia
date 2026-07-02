package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.TurmaResumoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.seguranca.UsuarioAutenticado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import gerenciamentoDeAcademia.util.CpfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoPortalAluno {

    private final TurmaRepository turmaRepository;
    private final AlunoRepository alunoRepository;

    @Transactional(readOnly = true)
    public List<TurmaResumoDto> listarMinhasTurmas(UsuarioAutenticado usuario) {
        ExcecaoDeDominio.quando(usuario == null || !usuario.isPortalAluno(), "Acesso exclusivo do portal do aluno.");
        Aluno aluno = alunoRepository.findByCpf(usuario.getUsername());
        ExcecaoDeDominio.quandoNulo(aluno, "Cadastro de aluno não encontrado.");

        String cpf = CpfUtil.somenteDigitos(aluno.getCpf());
        Long instituicaoId = usuario.getInstituicaoId();

        List<Turma> turmas;
        if (instituicaoId != null && instituicaoId > 0) {
            turmas = turmaRepository.findTurmasDoAlunoNaInstituicao(cpf, instituicaoId);
            if (turmas.isEmpty()) {
                turmas = turmaRepository.findTurmasMatriculadasPorCpf(cpf).stream()
                        .filter(t -> t.getInstituicao() != null && instituicaoId.equals(t.getInstituicao().getId()))
                        .toList();
            }
        } else {
            turmas = turmaRepository.findTurmasMatriculadasPorCpf(cpf);
        }

        return turmas.stream()
                .sorted(Comparator.comparing(Turma::getModalidade, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(TurmaResumoDto::of)
                .toList();
    }
}

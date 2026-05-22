package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoVinculoAlunoInstituicao {

    public static final String MODALIDADE_MATRICULA = "Matrícula institucional";

    private final InstituicaoRepository instituicaoRepository;
    private final TurmaRepository turmaRepository;

    @Transactional
    public void vincularAlunoNaInstituicao(Long instituicaoId, Aluno aluno) {
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno inválido.");
        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));
        ExcecaoDeDominio.quando(!Boolean.TRUE.equals(instituicao.getCadastroAtivo()), "Instituição inativa.");

        if (instituicaoRepository.alunoVinculadoInstituicao(aluno.getCpf(), instituicaoId)) {
            return;
        }

        Turma turma = turmaRepository.findFirstByInstituicao_IdAndModalidadeOrderByIdAsc(instituicaoId, MODALIDADE_MATRICULA)
                .orElseGet(() -> criarTurmaMatricula(instituicao));

        if (!turma.getAlunos().contains(aluno)) {
            turma.getAlunos().add(aluno);
            turmaRepository.save(turma);
        }
    }

    private Turma criarTurmaMatricula(Instituicao instituicao) {
        Turma turma = new Turma();
        turma.setInstituicao(instituicao);
        turma.setModalidade(MODALIDADE_MATRICULA);
        turma.setHorario("08:00-09:00");
        turma.setHoraInicio(LocalTime.of(8, 0));
        turma.setHoraFim(LocalTime.of(9, 0));
        turma.setDias(List.of("Segunda"));
        turma.setSala(null);
        return turmaRepository.save(turma);
    }
}

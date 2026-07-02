package gerenciamentoDeAcademia.servicos.turma;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mantém o vínculo bidirecional turma ↔ aluno (lado dono: {@link Turma#getAlunos()}).
 */
@Component
@RequiredArgsConstructor
public class VinculoTurmaAluno {

    private final AlunoRepository alunoRepository;

    public void vincular(Turma turma, Aluno aluno) {
        if (turma == null || aluno == null) {
            return;
        }
        Aluno gerenciado = aluno.getId() != null
                ? aluno
                : alunoRepository.findByCpf(aluno.getCpf());
        if (gerenciado == null) {
            return;
        }
        if (!turma.getAlunos().contains(gerenciado)) {
            turma.getAlunos().add(gerenciado);
        }
        if (!gerenciado.getTurma().contains(turma)) {
            gerenciado.getTurma().add(turma);
        }
    }

    public void desvincular(Turma turma, Aluno aluno) {
        if (turma == null || aluno == null) {
            return;
        }
        Aluno gerenciado = alunoRepository.findByCpf(aluno.getCpf());
        if (gerenciado == null) {
            return;
        }
        turma.getAlunos().remove(gerenciado);
        gerenciado.getTurma().remove(turma);
    }
}

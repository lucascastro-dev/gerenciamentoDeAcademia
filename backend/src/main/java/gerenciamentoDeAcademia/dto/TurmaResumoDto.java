package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Turma;

import java.util.List;

public record TurmaResumoDto(
        Long id,
        String modalidade,
        String horario,
        List<String> dias,
        String professorNome,
        String professorCpf,
        int totalAlunos
) {
    public static TurmaResumoDto of(Turma turma) {
        return new TurmaResumoDto(
                turma.getId(),
                turma.getModalidade(),
                turma.getHorario(),
                turma.getDias(),
                turma.getProfessor() != null ? turma.getProfessor().getNome() : null,
                turma.getProfessor() != null ? turma.getProfessor().getCpf() : null,
                turma.getAlunos() != null ? turma.getAlunos().size() : 0
        );
    }
}

package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaResumoDto {
    private Long id;
    private String modalidade;
    private String horario;
    private String sala;
    private List<String> dias = new ArrayList<>();
    private String horaInicio;
    private String horaFim;
    private Integer totalAlunos;
    private String professorNome;
    private String professorEspecializacao;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static TurmaResumoDto of(Turma turma) {
        TurmaResumoDto dto = new TurmaResumoDto();
        dto.setId(IdUtil.toLong(turma.getId()));
        dto.setModalidade(turma.getModalidade());
        dto.setSala(turma.getSala());
        dto.setDias(turma.getDias() != null ? new ArrayList<>(turma.getDias()) : new ArrayList<>());
        dto.setHoraInicio(formatarHora(turma.getHoraInicio()));
        dto.setHoraFim(formatarHora(turma.getHoraFim()));
        dto.setHorario(montarHorarioExibicao(turma));
        dto.setTotalAlunos(turma.getAlunos() != null ? turma.getAlunos().size() : 0);
        if (turma.getProfessor() != null) {
            dto.setProfessorNome(turma.getProfessor().getNome());
            dto.setProfessorEspecializacao(turma.getProfessor().getEspecializacao());
        }
        return dto;
    }

    private static String formatarHora(LocalTime hora) {
        return hora != null ? hora.format(HORA) : null;
    }

    private static String montarHorarioExibicao(Turma turma) {
        if (turma.getHoraInicio() != null && turma.getHoraFim() != null) {
            return turma.getHoraInicio().format(HORA) + "-" + turma.getHoraFim().format(HORA);
        }
        return turma.getHorario() != null ? turma.getHorario() : "";
    }
}

package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Turma;
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
public class TurmaListagemDto {
    private Long id;
    private String modalidade;
    private String horario;
    private String sala;
    private List<String> dias = new ArrayList<>();
    private ProfessorResumoDto professor;
    private InstituicaoResumoDto instituicao;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static TurmaListagemDto of(Turma turma) {
        TurmaListagemDto dto = new TurmaListagemDto();
        dto.setId(turma.getId());
        dto.setModalidade(turma.getModalidade());
        dto.setSala(turma.getSala());
        dto.setDias(turma.getDias() != null ? new ArrayList<>(turma.getDias()) : new ArrayList<>());
        dto.setHorario(montarHorarioExibicao(turma));
        if (turma.getProfessor() != null) {
            dto.setProfessor(ProfessorResumoDto.of(turma.getProfessor()));
        }
        if (turma.getInstituicao() != null) {
            dto.setInstituicao(new InstituicaoResumoDto(
                    turma.getInstituicao().getId(),
                    turma.getInstituicao().getRazaoSocial()));
        }
        return dto;
    }

    private static String montarHorarioExibicao(Turma turma) {
        if (turma.getHoraInicio() != null && turma.getHoraFim() != null) {
            return turma.getHoraInicio().format(HORA) + "-" + turma.getHoraFim().format(HORA);
        }
        return turma.getHorario() != null ? turma.getHorario() : "";
    }

}

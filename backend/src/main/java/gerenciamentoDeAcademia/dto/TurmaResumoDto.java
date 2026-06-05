package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Turma;
import gerenciamentoDeAcademia.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaResumoDto {
    private Long id;
    private String modalidade;
    private String horario;
    private String sala;

    public static TurmaResumoDto of(Turma turma) {
        return new TurmaResumoDto(
                IdUtil.toLong(turma.getId()),
                turma.getModalidade(),
                turma.getHorario(),
                turma.getSala()
        );
    }
}

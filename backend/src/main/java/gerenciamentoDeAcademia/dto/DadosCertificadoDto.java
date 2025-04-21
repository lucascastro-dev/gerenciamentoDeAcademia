package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DadosCertificadoDto {
    private String professor;
    private LocalDate dataEvento;
    private Boolean personalizado;
    private String projeto;
    private List<AlunoDto> alunos = new ArrayList<>();
}

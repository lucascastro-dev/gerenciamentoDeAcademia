package gerenciamentoDeAcademia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlunoMatriculaInstituicaoDto {
    private Long instituicaoId;
    private String razaoSocial;
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private LocalDate dataUltimoPagamentoMensalidade;
    private List<TurmaResumoDto> turmas = new ArrayList<>();
}

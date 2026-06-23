package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.EscopoLancamentoProgramacao;
import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ItemProgramacaoFormDto {
    private EscopoLancamentoProgramacao escopoLancamento;
    private String cpfAluno;
    private Long turmaId;
    private TipoItemProgramacao tipo;
    private String titulo;
    private String descricao;
    private LocalDate dataPrevista;
    private LocalDate dataFim;
    /** Legado; preferir horaInicio/horaFim. */
    private String horario;
    private String horaInicio;
    private String horaFim;
    private String sala;
}

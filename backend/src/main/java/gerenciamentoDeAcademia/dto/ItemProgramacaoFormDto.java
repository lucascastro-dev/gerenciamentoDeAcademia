package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.TipoItemProgramacao;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ItemProgramacaoFormDto {
    private String cpfAluno;
    private TipoItemProgramacao tipo;
    private String titulo;
    private String descricao;
    private LocalDate dataPrevista;
    private String horario;
    private String sala;
}

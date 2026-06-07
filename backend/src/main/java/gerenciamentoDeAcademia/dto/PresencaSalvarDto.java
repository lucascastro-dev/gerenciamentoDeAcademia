package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PresencaSalvarDto {
    private Integer ano;
    private Integer mes;
    private List<RegistroPresencaDto> registros = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RegistroPresencaDto {
        private String alunoCpf;
        private Integer dia;
        private String status;
    }
}

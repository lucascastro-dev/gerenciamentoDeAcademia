package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Sala;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalaDto {
    private Long id;
    private String nome;
    private Integer capacidade;
    private Boolean ativa;

    public static SalaDto of(Sala sala) {
        if (sala == null) {
            return null;
        }
        SalaDto dto = new SalaDto();
        dto.setId(sala.getId());
        dto.setNome(sala.getNome());
        dto.setCapacidade(sala.getCapacidade());
        dto.setAtiva(sala.getAtiva());
        return dto;
    }
}

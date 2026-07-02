package gerenciamentoDeAcademia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@AllArgsConstructor
public class ArquivoPdfDto {
    private final Resource resource;
    private final String nomeArquivo;
}

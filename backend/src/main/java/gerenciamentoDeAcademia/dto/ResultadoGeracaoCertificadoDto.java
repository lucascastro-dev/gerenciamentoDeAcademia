package gerenciamentoDeAcademia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResultadoGeracaoCertificadoDto {
    private String mensagem;
    private String nomeArquivoResumo;
    private String conteudoResumo;
}

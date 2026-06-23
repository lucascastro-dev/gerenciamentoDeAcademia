package gerenciamentoDeAcademia.dto;

import java.time.LocalDateTime;

public record StatusIntegracaoPontoDto(
        Integer mesCompetencia,
        Integer anoCompetencia,
        boolean pontoConferidoRh,
        LocalDateTime conferidoEm,
        String conferidoPorCpf,
        boolean integradoFinanceiro,
        LocalDateTime integradoEm,
        int colaboradoresComRegistro,
        long totalMinutosInstituicao
) {
}

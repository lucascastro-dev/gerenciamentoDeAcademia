package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.DocumentoRemuneracaoColaborador;
import gerenciamentoDeAcademia.enums.TipoDocumentoRemuneracao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DocumentoRemuneracaoDto(
        Long id,
        TipoDocumentoRemuneracao tipo,
        String tipoDescricao,
        String nomeColaborador,
        Integer mesCompetencia,
        Integer anoCompetencia,
        BigDecimal valorBruto,
        BigDecimal valorLiquido,
        String conteudo,
        LocalDateTime publicadoEm
) {
    public static DocumentoRemuneracaoDto of(DocumentoRemuneracaoColaborador doc) {
        if (doc == null) {
            return null;
        }
        return new DocumentoRemuneracaoDto(
                doc.getId(),
                doc.getTipo(),
                doc.getTipo() != null ? doc.getTipo().getDescricao() : null,
                doc.getNomeColaborador(),
                doc.getMesCompetencia(),
                doc.getAnoCompetencia(),
                doc.getValorBruto(),
                doc.getValorLiquido(),
                doc.getConteudo(),
                doc.getPublicadoEm()
        );
    }
}

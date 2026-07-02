package gerenciamentoDeAcademia.infra.integracoes;

import gerenciamentoDeAcademia.dto.integracoes.CriarCobrancaMensalidadeFormDto;
import gerenciamentoDeAcademia.enums.TipoCobrancaExterna;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitacaoCobrancaExterna(
        TipoCobrancaExterna tipo,
        Long instituicaoId,
        String cpfPagador,
        String nomePagador,
        String emailPagador,
        BigDecimal valor,
        String descricao,
        Integer mesCompetencia,
        Integer anoCompetencia,
        String billingType,
        LocalDate dueDate,
        CriarCobrancaMensalidadeFormDto.DadosCartaoCreditoFormDto cartao
) {
    public SolicitacaoCobrancaExterna(
            TipoCobrancaExterna tipo,
            Long instituicaoId,
            String cpfPagador,
            String nomePagador,
            String emailPagador,
            BigDecimal valor,
            String descricao,
            Integer mesCompetencia,
            Integer anoCompetencia) {
        this(tipo, instituicaoId, cpfPagador, nomePagador, emailPagador, valor, descricao,
                mesCompetencia, anoCompetencia, "UNDEFINED", LocalDate.now().plusDays(3), null);
    }
}

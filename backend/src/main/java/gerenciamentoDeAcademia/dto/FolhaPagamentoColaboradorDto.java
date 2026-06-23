package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.TipoFuncionario;

import java.math.BigDecimal;

public record FolhaPagamentoColaboradorDto(
        String cpf,
        String nome,
        TipoFuncionario tipoFuncionario,
        String cargo,
        BigDecimal salarioBase,
        String statusPagamento,
        boolean reciboPublicado,
        Integer diasTrabalhados,
        Long minutosTrabalhados,
        String horasTrabalhadasFormatadas,
        boolean pontoMesConferidoRh
) {
}

package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.SituacaoCobranca;
import gerenciamentoDeAcademia.enums.TipoAcesso;
import gerenciamentoDeAcademia.enums.TipoFuncionario;

import java.util.List;

public record LoginRetornoDto(
        String token,
        String nome,
        TipoFuncionario tipoFuncionario,
        String perfilExibicao,
        boolean usuarioMaster,
        boolean masterRaiz,
        List<String> permissoes,
        TipoAcesso tipoAcesso,
        boolean planoInstituicaoAtivo,
        boolean acessoFinanceiroCompleto,
        SituacaoCobranca situacaoCobranca,
        boolean alertaCobranca,
        String mensagemAlertaCobranca
) {
}

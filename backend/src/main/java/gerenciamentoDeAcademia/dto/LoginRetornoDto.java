package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.TipoAcesso;
import gerenciamentoDeAcademia.enums.TipoFuncionario;

import java.util.List;

public record LoginRetornoDto(
        String token,
        String nome,
        TipoFuncionario tipoFuncionario,
        boolean usuarioMaster,
        List<String> permissoes,
        TipoAcesso tipoAcesso,
        boolean planoInstituicaoAtivo
) {
}

package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.UserRole;

public record LoginRequisicaoDto(String login, String password, String vinculo, UserRole role) {
}

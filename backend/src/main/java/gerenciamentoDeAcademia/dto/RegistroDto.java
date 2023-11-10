package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.enums.UserRole;

public record RegistroDto(String login, String password, UserRole role) {
}

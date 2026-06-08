package gerenciamentoDeAcademia.util;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;

import java.util.regex.Pattern;

/**
 * Validação de e-mail alinhada ao frontend (pré-cadastro e formulários).
 */
public final class PoliticaEmail {

    private static final Pattern FORMATO = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+$");

    private PoliticaEmail() {
    }

    public static void validar(String email) {
        ExcecaoDeDominio.quandoNuloOuVazio(email, "E-mail é obrigatório");
        if (!ehValido(email)) {
            throw new ExcecaoDeDominio("E-mail em formato inválido. Informe um endereço como nome@dominio.com");
        }
    }

    public static boolean ehValido(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return FORMATO.matcher(email.trim()).matches();
    }

    public static String normalizar(String email) {
        return email == null ? null : email.trim();
    }
}

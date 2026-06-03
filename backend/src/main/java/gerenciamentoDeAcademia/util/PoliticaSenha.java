package gerenciamentoDeAcademia.util;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Política de senha forte alinhada ao frontend (cadastro e alteração de senha).
 */
public final class PoliticaSenha {

    private static final int TAMANHO_MINIMO = 8;
    private static final Pattern MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern DIGITO = Pattern.compile("\\d");
    private static final Pattern ESPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private PoliticaSenha() {
    }

    public static void validarSenhaForte(String senha) {
        ExcecaoDeDominio.quandoNuloOuVazio(senha, "Senha é obrigatória");
        List<String> pendencias = listarPendencias(senha);
        if (!pendencias.isEmpty()) {
            throw new ExcecaoDeDominio(
                    "Senha fraca. Requisitos: " + String.join("; ", pendencias));
        }
    }

    public static List<String> listarPendencias(String senha) {
        List<String> p = new ArrayList<>();
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            p.add("mínimo de 8 caracteres");
        }
        if (senha == null || !MAIUSCULA.matcher(senha).find()) {
            p.add("uma letra maiúscula");
        }
        if (senha == null || !MINUSCULA.matcher(senha).find()) {
            p.add("uma letra minúscula");
        }
        if (senha == null || !DIGITO.matcher(senha).find()) {
            p.add("um número");
        }
        if (senha == null || !ESPECIAL.matcher(senha).find()) {
            p.add("um caractere especial");
        }
        return p;
    }
}

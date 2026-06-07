package gerenciamentoDeAcademia.util;

public final class MascaramentoDadosUtil {

    private MascaramentoDadosUtil() {}

    public static String cpf(String cpf) {
        String digitos = CpfUtil.somenteDigitos(cpf);
        if (digitos.length() != 11) {
            return "***";
        }
        return "***." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-**";
    }

    public static String telefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return "";
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() < 4) {
            return "****";
        }
        String final4 = digitos.substring(digitos.length() - 4);
        if (digitos.length() >= 10) {
            String ddd = digitos.substring(0, 2);
            return "(" + ddd + ") *****-" + final4;
        }
        return "*****-" + final4;
    }

    public static String email(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "";
        }
        int arroba = email.indexOf('@');
        String local = email.substring(0, arroba);
        String dominio = email.substring(arroba + 1);
        String prefixo = local.length() <= 1 ? "*" : local.charAt(0) + "***";
        return prefixo + "@" + dominio;
    }

    public static String rg(String rg) {
        if (rg == null || rg.isBlank()) {
            return "";
        }
        String digitos = rg.replaceAll("\\D", "");
        if (digitos.length() < 3) {
            return "***";
        }
        return "***." + digitos.substring(Math.max(0, digitos.length() - 6));
    }
}

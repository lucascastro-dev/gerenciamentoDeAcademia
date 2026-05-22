package gerenciamentoDeAcademia.util;

public final class CpfUtil {

    private CpfUtil() {
    }

    public static String somenteDigitos(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }
}

package gerenciamentoDeAcademia.util;

/**
 * Normaliza identificadores numéricos retornados por JPA/Hibernate (ex.: {@link Integer} em campos {@link Long}).
 */
public final class IdUtil {

    private IdUtil() {
    }

    public static Long toLong(Object id) {
        if (id == null) {
            return null;
        }
        if (id instanceof Long longId) {
            return longId;
        }
        if (id instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(id.toString());
    }
}

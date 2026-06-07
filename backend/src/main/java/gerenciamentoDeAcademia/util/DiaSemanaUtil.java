package gerenciamentoDeAcademia.util;

import java.time.DayOfWeek;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DiaSemanaUtil {

    private static final Map<String, DayOfWeek> ALIASES = new HashMap<>();

    static {
        registrar(DayOfWeek.MONDAY, "segunda", "segunda-feira", "seg");
        registrar(DayOfWeek.TUESDAY, "terça", "terca", "terça-feira", "terca-feira", "ter");
        registrar(DayOfWeek.WEDNESDAY, "quarta", "quarta-feira", "qua");
        registrar(DayOfWeek.THURSDAY, "quinta", "quinta-feira", "qui");
        registrar(DayOfWeek.FRIDAY, "sexta", "sexta-feira", "sex");
        registrar(DayOfWeek.SATURDAY, "sábado", "sabado", "sáb", "sab");
        registrar(DayOfWeek.SUNDAY, "domingo", "dom");
    }

    private DiaSemanaUtil() {}

    private static void registrar(DayOfWeek dia, String... nomes) {
        for (String nome : nomes) {
            ALIASES.put(normalizar(nome), dia);
        }
    }

    public static Set<DayOfWeek> resolverDiasDaTurma(List<String> diasTurma) {
        if (diasTurma == null || diasTurma.isEmpty()) {
            return Set.of();
        }
        return diasTurma.stream()
                .map(DiaSemanaUtil::resolverDia)
                .filter(d -> d != null)
                .collect(Collectors.toSet());
    }

    public static DayOfWeek resolverDia(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return ALIASES.get(normalizar(valor));
    }

    private static String normalizar(String valor) {
        String semAcento = Normalizer.normalize(valor.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento;
    }
}

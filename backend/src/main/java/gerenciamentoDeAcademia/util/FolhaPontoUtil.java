package gerenciamentoDeAcademia.util;

import java.time.Duration;
import java.time.LocalDateTime;

public final class FolhaPontoUtil {

    private FolhaPontoUtil() {
    }

    public static long minutosTrabalhados(LocalDateTime entrada, LocalDateTime saida) {
        if (entrada == null || saida == null || saida.isBefore(entrada)) {
            return 0;
        }
        return Duration.between(entrada, saida).toMinutes();
    }

    public static String formatarHoras(long minutos) {
        if (minutos <= 0) {
            return "0h";
        }
        long horas = minutos / 60;
        long resto = minutos % 60;
        if (resto == 0) {
            return horas + "h";
        }
        return horas + "h " + resto + "min";
    }

    public static String formatarHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            return "—";
        }
        return String.format("%02d:%02d", dataHora.getHour(), dataHora.getMinute());
    }
}

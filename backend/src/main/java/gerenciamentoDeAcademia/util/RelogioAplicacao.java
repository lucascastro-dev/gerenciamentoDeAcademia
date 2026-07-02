package gerenciamentoDeAcademia.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Horário de referência da aplicação (Brasília). */
public final class RelogioAplicacao {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private RelogioAplicacao() {}

    public static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    public static LocalDateTime agora() {
        return LocalDateTime.now(FUSO);
    }
}

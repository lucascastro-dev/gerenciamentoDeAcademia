package gerenciamentoDeAcademia.util;

import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Interpreta horários no formato 18:00, 18:00-19:30 ou 18h00. */
public record IntervaloHorario(LocalTime inicio, LocalTime fim) {

    private static final Pattern RANGE = Pattern.compile(
            "(\\d{1,2})[:h]?(\\d{2})?\\s*[-–]\\s*(\\d{1,2})[:h]?(\\d{2})?");
    private static final Pattern SINGLE = Pattern.compile("(\\d{1,2})[:h]?(\\d{2})?");

    public static IntervaloHorario parse(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String t = texto.trim();
        Matcher range = RANGE.matcher(t);
        if (range.matches()) {
            LocalTime ini = LocalTime.of(
                    Integer.parseInt(range.group(1)),
                    range.group(2) != null ? Integer.parseInt(range.group(2)) : 0);
            LocalTime end = LocalTime.of(
                    Integer.parseInt(range.group(3)),
                    range.group(4) != null ? Integer.parseInt(range.group(4)) : 0);
            validarOrdem(ini, end);
            return new IntervaloHorario(ini, end);
        }
        Matcher single = SINGLE.matcher(t);
        if (single.matches()) {
            LocalTime ini = LocalTime.of(
                    Integer.parseInt(single.group(1)),
                    single.group(2) != null ? Integer.parseInt(single.group(2)) : 0);
            return new IntervaloHorario(ini, ini.plusHours(1));
        }
        throw new ExcecaoDeDominio("Horário inválido. Use ex.: 18:00 ou 18:00-19:30");
    }

    public boolean sobrepoe(IntervaloHorario outro) {
        if (outro == null) {
            return false;
        }
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }

    private static void validarOrdem(LocalTime ini, LocalTime end) {
        ExcecaoDeDominio.quando(!ini.isBefore(end), "Hora final deve ser posterior à inicial.");
    }
}

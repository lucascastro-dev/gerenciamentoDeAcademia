package gerenciamentoDeAcademia.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

class DiaSemanaUtilTest {

    @Test
    @DisplayName("Dado dias curtos Terça e Quinta, Quando resolver, Então mapeia corretamente")
    void deveResolverDiasCurtosCadastradosNaTurma() {
        Set<DayOfWeek> dias = DiaSemanaUtil.resolverDiasDaTurma(List.of("Terça", "Quinta"));
        Assertions.assertEquals(Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), dias);
    }

    @Test
    @DisplayName("Dado dias por extenso, Quando resolver, Então mapeia corretamente")
    void deveResolverDiasPorExtenso() {
        Set<DayOfWeek> dias = DiaSemanaUtil.resolverDiasDaTurma(List.of("segunda-feira", "quarta-feira"));
        Assertions.assertEquals(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), dias);
    }

    @Test
    @DisplayName("Dado Terça e Quinta em junho/2026, Quando calcular dias do mês, Então encontra aulas")
    void deveCalcularDiasDeJunho2026ParaTercaEQuinta() {
        Set<DayOfWeek> diasSemana = DiaSemanaUtil.resolverDiasDaTurma(List.of("Terça", "Quinta"));
        YearMonth junho = YearMonth.of(2026, 6);
        int encontrados = 0;
        for (int d = 1; d <= junho.lengthOfMonth(); d++) {
            if (diasSemana.contains(junho.atDay(d).getDayOfWeek())) {
                encontrados++;
            }
        }
        Assertions.assertEquals(9, encontrados);
    }
}

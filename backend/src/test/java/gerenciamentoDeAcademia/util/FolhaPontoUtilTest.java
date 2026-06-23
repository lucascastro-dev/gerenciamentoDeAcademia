package gerenciamentoDeAcademia.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class FolhaPontoUtilTest {

    @Test
    void deveCalcularMinutosEntreEntradaESaida() {
        LocalDateTime entrada = LocalDateTime.of(2026, 6, 3, 8, 0);
        LocalDateTime saida = LocalDateTime.of(2026, 6, 3, 17, 30);
        Assertions.assertEquals(570, FolhaPontoUtil.minutosTrabalhados(entrada, saida));
        Assertions.assertEquals("9h 30min", FolhaPontoUtil.formatarHoras(570));
    }

    @Test
    void deveRetornarZeroQuandoSaidaAusente() {
        LocalDateTime entrada = LocalDateTime.of(2026, 6, 3, 8, 0);
        Assertions.assertEquals(0, FolhaPontoUtil.minutosTrabalhados(entrada, null));
    }
}

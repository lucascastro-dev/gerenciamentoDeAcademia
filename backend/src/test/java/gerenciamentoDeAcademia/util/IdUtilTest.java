package gerenciamentoDeAcademia.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IdUtilTest {

    @Test
    void deveConverterIntegerParaLong() {
        Assertions.assertEquals(1L, IdUtil.toLong(Integer.valueOf(1)));
    }

    @Test
    void deveManterLong() {
        Assertions.assertEquals(42L, IdUtil.toLong(42L));
    }

    @Test
    void deveRetornarNullParaNull() {
        Assertions.assertNull(IdUtil.toLong(null));
    }
}

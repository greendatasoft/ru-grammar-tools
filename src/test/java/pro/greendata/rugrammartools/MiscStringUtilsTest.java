package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.greendata.rugrammartools.impl.MiscStringUtils;

/**
 * Created by @ssz on 25.02.2022.
 */
public class MiscStringUtilsTest {

    @Test
    public void testToProperCase() {
        String orig1 = "ТесТ-ТеСтъ";
        String res1 = "тест-тесты";
        String actual1 = MiscStringUtils.toProperCase(orig1, res1);
        Assertions.assertEquals("ТесТ-ТеСты", actual1);

        String orig2 = "Один рубль";
        String res2 = "одного рублёв";
        String actual2 = MiscStringUtils.toProperCase(orig2, res2);
        Assertions.assertEquals("Одного рублёв", actual2);
    }
}

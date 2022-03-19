package pro.greendata.rugrammartools.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

/**
 * Created by @ssz on 25.02.2022.
 */
public class TextUtilsTest {

    @Test
    public void testToProperCase1() {
        String orig1 = "ТесТ-ТеСтъ";
        String res1 = "тест-тесты";
        String actual1 = TextUtils.toProperCase(orig1, res1);
        Assertions.assertEquals("ТесТ-ТеСты", actual1);

        String orig2 = "Один рубль";
        String res2 = "одного рублёв";
        String actual2 = TextUtils.toProperCase(orig2, res2);
        Assertions.assertEquals("Одного рублёв", actual2);
    }

    @Test
    public void testToProperCase2() {
        String orig1 = "xxxY";
        String res1 = "xxxy";
        String actual1 = TextUtils.toProperCase(orig1, res1);
        Assertions.assertEquals("xxxY", actual1);

        String orig2 = "ZZZf";
        String res2 = "zzzH";
        String actual2 = TextUtils.toProperCase(orig2, res2);
        Assertions.assertEquals("ZZZh", actual2);
    }
}

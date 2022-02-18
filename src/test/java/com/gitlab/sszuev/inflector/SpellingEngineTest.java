package com.gitlab.sszuev.inflector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by @ssz on 18.02.2022.
 */
public class SpellingEngineTest {

    private final SpellingEngine spellingEngine = TestUtils.createSpellingEngine();

    @Test
    public void testSmallIntegers() {
        Assertions.assertEquals("минус сорок два", spellingEngine.spell(-42));
        Assertions.assertEquals("сто двадцать три", spellingEngine.spell(123));
        Assertions.assertEquals("девятнадцать", spellingEngine.spell(19));
        Assertions.assertEquals("сто одиннадцать", spellingEngine.spell(111));
        Assertions.assertEquals("триста два", spellingEngine.spell(302));
        Assertions.assertEquals("пятьсот двадцать", spellingEngine.spell(520));
    }

    @Test
    public void testBigIntegers() {
        Assertions.assertEquals("двадцать пять тысяч", spellingEngine.spell(25_000));
        Assertions.assertEquals("двадцать шесть тысяч девятьсот девяносто девять", spellingEngine.spell(26_999));
        Assertions.assertEquals("сорок тысяч", spellingEngine.spell(40_000));
        Assertions.assertEquals("девятьсот тысяч", spellingEngine.spell(900_000));
        Assertions.assertEquals("один миллион двадцать четыре тысячи сто одиннадцать", spellingEngine.spell(1_024_111));
        Assertions.assertEquals("сто двадцать один миллион сто двадцать две тысячи четыреста пятьдесят шесть",
                spellingEngine.spell(121_122_456));
        Assertions.assertEquals("четыреста двадцать два миллиарда двадцать семь тысяч двести двадцать",
                spellingEngine.spell(422_000_027_220.));
        Assertions.assertEquals("один квадриллион три триллиона два миллиарда сорок две тысячи триста",
                spellingEngine.spell(new BigDecimal(new BigInteger("1003002000042300"))));
    }
}
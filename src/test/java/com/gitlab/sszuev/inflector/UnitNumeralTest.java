package com.gitlab.sszuev.inflector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

/**
 * Created by @ssz on 21.02.2022.
 */
public class UnitNumeralTest {

    private final InflectionEngine engine = TestUtils.createInflectorEngine();

    public static List<String[]> data() {
        return TestUtils.load("unit_numerals.txt");
    }

    void assertPhrase(String expected, String givenNumeral, String givenUnit, Case declension) {
        Assertions.assertEquals(expected, engine.inflectNumeral(givenNumeral, givenUnit, declension), "Wrong result for case: " + declension);
    }

    @ParameterizedTest(name = "[{index}] ::: {1}")
    @MethodSource("data")
    public void testUnitNumerical(String p0, String p1, String p2, String p3, String p4, String p5, String p6) {
        String[] num = p0.substring(1, p0.length() - 1).split("\\|");
        assertPhrase(p1, num[0], num[1], Case.NOMINATIVE);
        assertPhrase(p2, num[0], num[1], Case.GENITIVE);
        assertPhrase(p3, num[0], num[1], Case.DATIVE);
        assertPhrase(p4, num[0], num[1], Case.ACCUSATIVE);
        assertPhrase(p5, num[0], num[1], Case.INSTRUMENTAL);
        assertPhrase(p6, num[0], num[1], Case.PREPOSITIONAL);
    }
}

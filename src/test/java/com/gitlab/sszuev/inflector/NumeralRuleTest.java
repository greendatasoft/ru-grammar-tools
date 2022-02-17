package com.gitlab.sszuev.inflector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by @ssz on 17.02.2022.
 */
public class NumeralRuleTest {

    private final InflectorEngine engine = TestUtils.createEngine();

    public static List<String[]> data() {
        return TestUtils.load("numerals.txt");
    }

    void assertPhrase(String expected, String given, Case declension, BiFunction<String, Case, String> inflector) {
        Assertions.assertEquals(expected, inflector.apply(given, declension), "Wrong result for case: " + declension);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("data")
    public void testNumerical(String p1, String p2, String p3, String p4, String p5, String p6) {
        testNumerical(engine::inflectNumeral, p1, p2, p3, p4, p5, p6);
    }

    protected void testNumerical(BiFunction<String, Case, String> inflector,
                                 String p1, String p2, String p3, String p4, String p5, String p6) {
        assertPhrase(p2, p1, Case.GENITIVE, inflector);
        assertPhrase(p3, p1, Case.DATIVE, inflector);
        assertPhrase(p4, p1, Case.ACCUSATIVE, inflector);
        assertPhrase(p5, p1, Case.INSTRUMENTAL, inflector);
        assertPhrase(p6, p1, Case.PREPOSITIONAL, inflector);
    }

    @Test
    @DisplayName("[test] ::: один")
    public void testNumeralMaleOne() {
        testNumerical((s, c) -> engine.inflect(s, WordType.NUMERALS, Gender.MALE, c), "один", "одного", "одному", "один", "одним", "одном");
    }
}

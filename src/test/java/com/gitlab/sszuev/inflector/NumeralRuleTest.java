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
 *
 * @see <a href='https://numeralonline.ru/1'>Склонение числительных</a>
 * @see <a href='https://ru.wikipedia.org/wiki/%D0%98%D0%BC%D0%B5%D0%BD%D0%BD%D1%8B%D0%B5_%D0%BD%D0%B0%D0%B7%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F_%D1%81%D1%82%D0%B5%D0%BF%D0%B5%D0%BD%D0%B5%D0%B9_%D1%82%D1%8B%D1%81%D1%8F%D1%87%D0%B8'>Именные названия степеней тысячи</a>
 */
public class NumeralRuleTest {

    private final InflectionEngine engine = TestUtils.createInflectorEngine();

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
        testNumerical((s, c) -> engine.inflect(s, WordType.NUMERALS, c, Gender.MALE, false),
                "один", "одного", "одному", "один", "одним", "одном");
    }

    @Test
    @DisplayName("[test] ::: одна")
    public void testNumeralFemaleOne() {
        testNumerical((s, c) -> engine.inflect(s, WordType.NUMERALS, c, Gender.FEMALE, false),
                "одна", "одной", "одной", "одну", "одной", "одной");
    }

    @Test
    @DisplayName("[test] ::: одно")
    public void testNumeralNeuterOne() {
        testNumerical((s, c) -> engine.inflect(s, WordType.NUMERALS, c, Gender.NEUTER, false),
                "одно", "одного", "одному", "одно", "одним", "одном");
    }

    @Test
    @DisplayName("[test] ::: тысяча")
    public void testNumeralThousand() {
        testNumerical((s, c) -> engine.inflect(s, WordType.NUMERALS, c, Gender.FEMALE, false),
                "тысяча", "тысячи", "тысяче", "тысячу", "тысячей", "тысяче");
    }

    @Test
    @DisplayName("[test] ::: тысяч")
    public void testNumeralPluralThousandFirstForm() {
        testNumerical((s, c) -> engine.inflect(s, WordType.NUMERALS, c, Gender.FEMALE, true),
                "тысяч", "тысяч", "тысячам", "тысяч", "тысячами", "тысячах");
    }

    @Test
    @DisplayName("[test] ::: тысячи")
    public void testNumeralPluralThousandSecondForm() {
        testNumerical((s, c) -> engine.inflect(s, WordType.NUMERALS, c, Gender.FEMALE, true),
                "тысячи", "тысяч", "тысячам", "тысячи", "тысячами", "тысячах");
    }
}

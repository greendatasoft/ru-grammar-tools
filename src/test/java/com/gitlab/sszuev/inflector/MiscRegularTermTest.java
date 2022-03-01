package com.gitlab.sszuev.inflector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

/**
 * Created by @ssz on 25.02.2022.
 */
public class MiscRegularTermTest {
    private final InflectionEngine engine = GrammarTools.getInflectionEngine();

    public static List<String[]> miscTerms() {
        return TestUtils.load("misc_terms.txt");
    }

    public static List<String[]> anyPhrases() {
        return TestUtils.load("any_phrases.txt");
    }

    void assertRegularTerm(String expected, String given, Case declension) {
        Assertions.assertEquals(expected, engine.inflectRegularTerm(given, declension, null),
                "Wrong result for case: " + declension);
    }

    void assertAnyPhrases(String expected, String given, Case declension) {
        Assertions.assertEquals(expected, engine.inflectAny(given, declension),
                "Wrong result for case: " + declension);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("miscTerms")
    public void testRegularTerms(String p1, String p2, String p3, String p4, String p5, String p6) {
        assertRegularTerm(p2, p1, Case.GENITIVE);
        assertRegularTerm(p3, p1, Case.DATIVE);
        assertRegularTerm(p4, p1, Case.ACCUSATIVE);
        assertRegularTerm(p5, p1, Case.INSTRUMENTAL);
        assertRegularTerm(p6, p1, Case.PREPOSITIONAL);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("anyPhrases")
    public void testAnyPhrases(String p1, String p2, String p3, String p4, String p5, String p6) {
        assertAnyPhrases(p2, p1, Case.GENITIVE);
        assertAnyPhrases(p3, p1, Case.DATIVE);
        assertAnyPhrases(p4, p1, Case.ACCUSATIVE);
        assertAnyPhrases(p5, p1, Case.INSTRUMENTAL);
        assertAnyPhrases(p6, p1, Case.PREPOSITIONAL);
    }
}

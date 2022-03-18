package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.BiFunction;

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

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("miscTerms")
    public void testRegularTerms(String p1, String p2, String p3, String p4, String p5, String p6) {
        testInflect((s, x) -> engine.inflectRegularTerm(s, x, null), p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("anyPhrases")
    public void testAnyPhrases(String p1, String p2, String p3, String p4, String p5, String p6) {
        testInflect(engine::inflectAny, p1, p2, p3, p4, p5, p6);
    }

    private void testInflect(BiFunction<String, Case, String> inflector,
                             String p1, String p2, String p3, String p4, String p5, String p6) {
        assertInflect(inflector, p2, p1, Case.GENITIVE);
        assertInflect(inflector,p3, p1, Case.DATIVE);
        assertInflect(inflector,p4, p1, Case.ACCUSATIVE);
        assertInflect(inflector,p5, p1, Case.INSTRUMENTAL);
        assertInflect(inflector,p6, p1, Case.PREPOSITIONAL);
    }

    private void assertInflect(BiFunction<String, Case, String> inflector, String expected, String given, Case declension) {
        Assertions.assertEquals(expected, inflector.apply(given, declension), "Wrong result for case: " + declension);
    }
}

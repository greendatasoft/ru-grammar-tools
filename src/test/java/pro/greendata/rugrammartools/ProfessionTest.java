package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

/**
 * Created by @ssz on 27.11.2020.
 */
public class ProfessionTest {

    private final InflectionEngine engine = GrammarTools.getInflectionEngine();

    public static List<String[]> data() {
        return TestUtils.load("professions.txt");
    }

    void assertName(String expected, String given, Case declension) {
        String actual = engine.inflectNameOfProfession(given, declension);
        Assertions.assertTrue(TestUtils.equalsIgnoreSpecial(expected, actual),
                String.format("Wrong result for case: %s, expected='%s', actual='%s'", declension, expected, actual));
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("data")
    public void testProfession(String p1, String p2, String p3, String p4, String p5, String p6) {
        assertName(p2, p1, Case.GENITIVE);
        assertName(p3, p1, Case.DATIVE);
        assertName(p4, p1, Case.ACCUSATIVE);
        assertName(p5, p1, Case.INSTRUMENTAL);
        assertName(p6, p1, Case.PREPOSITIONAL);
    }

}

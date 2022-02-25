package com.gitlab.sszuev.inflector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

/**
 * Created by @ssz on 24.02.2022.
 */
public class OrganizationRuleTest {

    private final InflectionEngine engine = TestUtils.createInflectorEngine();

    public static List<String[]> data() {
        return TestUtils.load("organization.txt");
    }

    void assertName(String expected, String given, Case declension) {
        Assertions.assertEquals(expected, engine.inflectRegularTerm(given, declension, false),
                "Wrong result for case: " + declension);
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

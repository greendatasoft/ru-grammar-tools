package com.gitlab.sszuev.inflector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

/**
 * Created by @ssz on 27.11.2020.
 *
 * @see <a href='https://github.com/petrovich4j/petrovich4j/blob/master/src/test/java/com/github/petrovich4j/PetrovichTests.java'>PetrovichTests</a>
 */
public class NameRuleTest {
    private final InflectionEngine engine = TestUtils.createInflectorEngine();

    private void check(WordType type, Gender gender, String... test) {
        for (Case c : Case.values()) {
            if (c == Case.NOMINATIVE) continue;
            checkName(test[c.ordinal()], test[0], type, gender, c);
        }
    }

    private void checkName(String expected, String given, WordType type, Gender gender, Case declension) {
        String actual = engine.inflect(given, type, declension, gender, false);
        actual = actual.replace("ё", "е");
        expected = expected.replace("ё", "е");
        Assertions.assertEquals(expected, actual, String.format("%s case, %s", declension, gender));
    }

    public static List<String[]> femaleFirstNameData() {
        return TestUtils.load("first_names_female.txt");
    }

    public static List<String[]> maleFirstNameData() {
        return TestUtils.load("first_names_male.txt");
    }

    public static List<String[]> femaleLastNameData() {
        return TestUtils.load("last_names_female.txt");
    }

    public static List<String[]> maleLastNameData() {
        return TestUtils.load("last_names_male.txt");
    }

    public static List<String[]> femalePatronymicNameData() {
        return TestUtils.load("patronymic_names_female.txt");
    }

    public static List<String[]> malePatronymicNameData() {
        return TestUtils.load("patronymic_names_male.txt");
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("femaleFirstNameData")
    public void testFemaleFirstNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(WordType.FIRST_NAME, Gender.FEMALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("maleFirstNameData")
    public void testMaleFirstNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(WordType.FIRST_NAME, Gender.MALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("femaleLastNameData")
    public void testFemaleLastNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(WordType.FAMILY_NAME, Gender.FEMALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("maleLastNameData")
    public void testMaleLastNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(WordType.FAMILY_NAME, Gender.MALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("femalePatronymicNameData")
    public void testFemalePatronymicNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(WordType.PATRONYMIC_NAME, Gender.FEMALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("malePatronymicNameData")
    public void testMalePatronymicNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(WordType.PATRONYMIC_NAME, Gender.MALE, p1, p2, p3, p4, p5, p6);
    }

    @Test
    public void checkLatinName() {
        check(WordType.FIRST_NAME, Gender.MALE, "John", "John", "John", "John", "John", "John");
    }
}

package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static pro.greendata.rugrammartools.TestUtils.load;

/**
 * Created by @ssz on 27.11.2020.
 *
 * @see <a href='https://github.com/petrovich4j/petrovich4j/blob/master/src/test/java/com/github/petrovich4j/PetrovichTests.java'>PetrovichTests</a>
 */
public class NamesTest extends SPFTestBase {

    private void check(SPFTestBase.SPF type, Gender gender, String... test) {
        for (Case c : Case.values()) {
            if (c == Case.NOMINATIVE) continue;
            checkName(test[c.ordinal()], test[0], type, gender, c);
        }
    }

    private void checkName(String expected, String given, SPFTestBase.SPF type, Gender gender, Case declension) {
        String actual = inflectName(given, type, declension, gender);
        actual = actual.replace("ё", "е");
        expected = expected.replace("ё", "е");
        Assertions.assertEquals(expected, actual, String.format("%s case, %s", declension, gender));
    }

    public static List<String[]> femaleFirstNameData() {
        return load("first_names_female.txt");
    }

    public static List<String[]> maleFirstNameData() {
        return load("first_names_male.txt");
    }

    public static List<String[]> femaleLastNameData() {
        return load("last_names_female.txt");
    }

    public static List<String[]> maleLastNameData() {
        return load("last_names_male.txt");
    }

    public static List<String[]> femalePatronymicNameData() {
        return load("patronymic_names_female.txt");
    }

    public static List<String[]> malePatronymicNameData() {
        return load("patronymic_names_male.txt");
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("femaleFirstNameData")
    public void testFemaleFirstNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(SPFTestBase.SPF.FIRSTNAME, Gender.FEMALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("maleFirstNameData")
    public void testMaleFirstNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(SPFTestBase.SPF.FIRSTNAME, Gender.MALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("femaleLastNameData")
    public void testFemaleLastNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(SPFTestBase.SPF.LASTNAME, Gender.FEMALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("maleLastNameData")
    public void testMaleLastNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(SPFTestBase.SPF.LASTNAME, Gender.MALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("femalePatronymicNameData")
    public void testFemalePatronymicNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(SPFTestBase.SPF.MIDDLENAME, Gender.FEMALE, p1, p2, p3, p4, p5, p6);
    }

    @ParameterizedTest(name = "[{index}] ::: {0}")
    @MethodSource("malePatronymicNameData")
    public void testMalePatronymicNames(String p1, String p2, String p3, String p4, String p5, String p6) {
        check(SPFTestBase.SPF.MIDDLENAME, Gender.MALE, p1, p2, p3, p4, p5, p6);
    }

    @Test
    public void checkLatinName() {
        check(SPFTestBase.SPF.FIRSTNAME, Gender.MALE, "John", "John", "John", "John", "John", "John");
    }
}

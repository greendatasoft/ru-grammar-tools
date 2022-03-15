package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by @ssz on 27.11.2020.
 *
 * @see <a href='https://github.com/petrovich4j/petrovich4j/blob/master/src/test/java/com/github/petrovich4j/RegressionTests.java'>com.github.petrovich4j.RegressionTests</a>
 */
public class MiscNameTest extends SPFTestBase {

    private void checkName(SPF type, Gender gender, String p1, String p2, String p3, String p4, String p5, String p6) {
        Assertions.assertEquals(p2, inflectName(p1, type, Case.GENITIVE, gender));
        Assertions.assertEquals(p3, inflectName(p1, type, Case.DATIVE, gender));
        Assertions.assertEquals(p4, inflectName(p1, type, Case.ACCUSATIVE, gender));
        Assertions.assertEquals(p5, inflectName(p1, type, Case.INSTRUMENTAL, gender));
        Assertions.assertEquals(p6, inflectName(p1, type, Case.PREPOSITIONAL, gender));
    }

    @Test
    public void testIssue1() {
        checkName(SPF.FIRSTNAME, Gender.MALE, "Паша", "Паши", "Паше", "Пашу", "Пашей", "Паше");
    }

    @Test
    public void testIssue2() {
        checkName(SPF.FIRSTNAME, Gender.MALE, "Павел", "Павла", "Павлу", "Павла", "Павлом", "Павле");
    }

    @Test
    public void testIssue3() {
        checkName(SPF.LASTNAME, Gender.FEMALE, "Ковалёва", "Ковалёвой", "Ковалёвой", "Ковалёву", "Ковалёвой", "Ковалёвой");
    }

    @Test
    public void testIssue4() {
        checkName(SPF.MIDDLENAME, Gender.MALE, "Ильич", "Ильича", "Ильичу", "Ильича", "Ильичом", "Ильиче");
    }

    @Test
    public void testIssue5() {
        // правильно как Ия,Ии,Ие,Ию,Ией,Ие, так и Ия,Ии,Ии,Ию,Ией,Ии
        // https://github.com/petrovich/petrovich-rules/issues/46
        checkName(SPF.FIRSTNAME, Gender.FEMALE, "Ия", "Ии", "Ии", "Ию", "Ией", "Ии");
    }

    @Test
    public void testIssue6() {
        checkName(SPF.LASTNAME, Gender.MALE, "Муромец", "Муромца", "Муромцу", "Муромца", "Муромцем", "Муромце");
    }

    @Test
    public void testIssue7() {
        checkName(SPF.FIRSTNAME, Gender.MALE, "Санёк", "Санька", "Саньку", "Санька", "Саньком", "Саньке");
    }

    @Test
    public void testIssue8() {
        checkName(SPF.FIRSTNAME, Gender.MALE, "Франсуа", "Франсуа", "Франсуа", "Франсуа", "Франсуа", "Франсуа");
    }

    @Test
    public void testIssue9() {
        checkName(SPF.FIRSTNAME, Gender.FEMALE, "Айгюль", "Айгюль", "Айгюль", "Айгюль", "Айгюль", "Айгюль");
    }
}

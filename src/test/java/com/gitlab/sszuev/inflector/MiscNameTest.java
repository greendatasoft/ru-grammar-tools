package com.gitlab.sszuev.inflector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by @ssz on 27.11.2020.
 *
 * @see <a href='https://github.com/petrovich4j/petrovich4j/blob/master/src/test/java/com/github/petrovich4j/RegressionTests.java'>com.github.petrovich4j.RegressionTests</a>
 */
public class MiscNameTest {
    private final InflectionEngine engine = GrammarTools.getInflectionEngine();

    private void checkName(WordType type, Gender gender, String p1, String p2, String p3, String p4, String p5, String p6) {
        Assertions.assertEquals(p2, engine.inflect(p1, type, Case.GENITIVE, gender, true, false));
        Assertions.assertEquals(p3, engine.inflect(p1, type, Case.DATIVE, gender, true, false));
        Assertions.assertEquals(p4, engine.inflect(p1, type, Case.ACCUSATIVE, gender, true, false));
        Assertions.assertEquals(p5, engine.inflect(p1, type, Case.INSTRUMENTAL, gender, true, false));
        Assertions.assertEquals(p6, engine.inflect(p1, type, Case.PREPOSITIONAL, gender, true, false));
    }

    @Test
    public void testIssue1() {
        checkName(WordType.FIRST_NAME, Gender.MALE, "Паша", "Паши", "Паше", "Пашу", "Пашей", "Паше");
    }

    @Test
    public void testIssue2() {
        checkName(WordType.FIRST_NAME, Gender.MALE, "Павел", "Павла", "Павлу", "Павла", "Павлом", "Павле");
    }

    @Test
    public void testIssue3() {
        checkName(WordType.FAMILY_NAME, Gender.FEMALE, "Ковалёва", "Ковалёвой", "Ковалёвой", "Ковалёву", "Ковалёвой", "Ковалёвой");
    }

    @Test
    public void testIssue4() {
        checkName(WordType.PATRONYMIC_NAME, Gender.MALE, "Ильич", "Ильича", "Ильичу", "Ильича", "Ильичом", "Ильиче");
    }

    @Test
    public void testIssue5() {
        // правильно как Ия,Ии,Ие,Ию,Ией,Ие, так и Ия,Ии,Ии,Ию,Ией,Ии
        // https://github.com/petrovich/petrovich-rules/issues/46
        checkName(WordType.FIRST_NAME, Gender.FEMALE, "Ия", "Ии", "Ии", "Ию", "Ией", "Ии");
    }

    @Test
    public void testIssue6() {
        checkName(WordType.FAMILY_NAME, Gender.MALE, "Муромец", "Муромца", "Муромцу", "Муромца", "Муромцем", "Муромце");
    }

    @Test
    public void testIssue7() {
        checkName(WordType.FIRST_NAME, Gender.MALE, "Санёк", "Санька", "Саньку", "Санька", "Саньком", "Саньке");
    }

    @Test
    public void testIssue8() {
        checkName(WordType.FIRST_NAME, Gender.MALE, "Франсуа", "Франсуа", "Франсуа", "Франсуа", "Франсуа", "Франсуа");
    }

    @Test
    public void testIssue9() {
        checkName(WordType.FIRST_NAME, Gender.FEMALE, "Айгюль", "Айгюль", "Айгюль", "Айгюль", "Айгюль", "Айгюль");
    }
}

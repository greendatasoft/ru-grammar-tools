package pro.greendata.rugrammartools.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.greendata.rugrammartools.Gender;

import java.util.List;

/**
 * Created by @ssz on 19.03.2022.
 */
public class PhraseTest {

    @Test
    public void testParsePhrase1() {
        String s = " AA BbB \tGggG ";
        Phrase p = Phrase.parse(s, null, null);
        Assertions.assertEquals(List.of(" ", " ", " \t", " "), p.separators);
        Assertions.assertEquals(List.of("AA", "BbB", "GggG"), p.words);
        Assertions.assertEquals(List.of("aa", "bbb", "gggg"), p.keys);
        Assertions.assertEquals(3, p.details.size());
        Assertions.assertNull(p.gender);
        Assertions.assertNull(p.animate);
        Assertions.assertEquals(s, p.compose());
    }

    @Test
    public void testParsePhrase2() {
        String s = "ааА\tБ\tввв ' Ддд ззз жжж'\n";
        Phrase p = Phrase.parse(s, Gender.NEUTER, false);
        Assertions.assertEquals(List.of("", "\t", "\t", " ", "\n"), p.separators);
        Assertions.assertEquals(List.of("ааА", "Б", "ввв", "' Ддд ззз жжж'"), p.words);
        Assertions.assertEquals(List.of("ааа", "б", "ввв", "' ддд ззз жжж'"), p.keys);
        Assertions.assertEquals(4, p.details.size());
        Assertions.assertEquals(Gender.NEUTER, p.gender);
        Assertions.assertFalse(p.animate);
        Assertions.assertEquals(s, p.compose());
    }

    @Test
    public void testMutableParsePhrase() {
        String s = "A b C";
        Phrase.Mutable p = Phrase.parse(s, Gender.NEUTER, false).toMutable();
        Assertions.assertEquals(s, p.compose());
        p.set(0, "d");
        Assertions.assertEquals("D b C", p.compose());

        p.set(1, "G");
        Assertions.assertEquals("D g C", p.compose());
    }
}

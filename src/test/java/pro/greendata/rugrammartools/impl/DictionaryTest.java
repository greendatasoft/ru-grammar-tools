package pro.greendata.rugrammartools.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;

/**
 * Created by @ssz on 24.02.2022.
 */
public class DictionaryTest {

    @Test
    public void testIsAnimate() {
        Word w1 = Dictionary.getNounDictionary().wordDetails("сапог").orElseThrow(AssertionError::new);
        Word w2 = Dictionary.getNounDictionary().wordDetails("птица").orElseThrow(AssertionError::new);
        Assertions.assertEquals(false, w1.animate());
        Assertions.assertEquals(true, w2.animate());
        Assertions.assertEquals(Gender.MALE, w1.gender());
        Assertions.assertEquals(Gender.FEMALE, w2.gender());
    }
}

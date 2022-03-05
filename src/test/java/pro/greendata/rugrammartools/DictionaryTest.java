package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.greendata.rugrammartools.impl.Dictionary;

/**
 * Created by @ssz on 24.02.2022.
 */
public class DictionaryTest {

    @Test
    public void testInflect() {
        Assertions.assertEquals("стороны", Dictionary.getNounDictionary().inflect("сторона", Case.GENITIVE, null, null, null));
        Assertions.assertEquals("стороной", Dictionary.getNounDictionary().inflect("сторона", Case.INSTRUMENTAL, null, null, false));
        Assertions.assertEquals("сторонам", Dictionary.getNounDictionary().inflect("сторона", Case.DATIVE, null, null, true));
    }

    @Test
    public void testIsAnimate() {
        Dictionary.Word w1 = Dictionary.getNounDictionary().wordInfo("сапог").orElseThrow(AssertionError::new);
        Dictionary.Word w2 = Dictionary.getNounDictionary().wordInfo("птица").orElseThrow(AssertionError::new);
        Assertions.assertEquals(false, w1.animate());
        Assertions.assertEquals(true, w2.animate());
        Assertions.assertEquals(Gender.MALE, w1.gender());
        Assertions.assertEquals(Gender.FEMALE, w2.gender());
    }
}

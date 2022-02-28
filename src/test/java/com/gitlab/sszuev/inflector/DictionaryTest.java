package com.gitlab.sszuev.inflector;

import com.gitlab.sszuev.inflector.impl.Dictionary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by @ssz on 24.02.2022.
 */
public class DictionaryTest {

    @Test
    public void testInflect() {
        Assertions.assertEquals("стороны", Dictionary.getInstance().inflect("сторона", Case.GENITIVE, null, null, null));
        Assertions.assertEquals("стороной", Dictionary.getInstance().inflect("сторона", Case.INSTRUMENTAL, null, null, false));
        Assertions.assertEquals("сторонам", Dictionary.getInstance().inflect("сторона", Case.DATIVE, null, null, true));
    }
}

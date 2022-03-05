package pro.greendata.rugrammartools;

import pro.greendata.rugrammartools.impl.InflectionEngineImpl;
import pro.greendata.rugrammartools.impl.SpellingEngineImpl;

/**
 * A factory to obtain grammar tools instances.
 * Created by @ssz on 01.03.2022.
 */
public class GrammarTools {

    /**
     * Creates and returns a facility for inflecting words and phrases into specified declension case.
     *
     * @return {@link InflectionEngine}
     */
    public static InflectionEngine getInflectionEngine() {
        return new InflectionEngineImpl();
    }

    /**
     * Creates and returns a facility for translating different objects (numbers right now) into russian words.
     *
     * @return {@link SpellingEngine}
     */
    public static SpellingEngine getSpellingEngine() {
        return new SpellingEngineImpl();
    }
}

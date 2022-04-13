package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;

/**
 * Represents a word (noun, adjective) details.
 * A part of {@link Phrase}.
 *
 * @see PartOfSpeech
 */
public interface Word {
    Gender gender();

    Boolean animate();

    PartOfSpeech partOfSpeech();

    Boolean isPlural();

    boolean isIndeclinable();

    default RuleType rule() {
        return RuleType.GENERIC;
    }

    default Dictionary.Record record() {
        return null;
    }
}

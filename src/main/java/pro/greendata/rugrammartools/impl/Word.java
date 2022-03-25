package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.PartOfSpeech;

/**
 * Represents a word (noun, adjective) details.
 * A part of {@link Phrase}.
 * @see PartOfSpeech
 */
public interface Word {
    Gender gender();

    Boolean animate();

    PartOfSpeech partOfSpeech();

    boolean isIndeclinable();

    default RuleType rule() {
        return RuleType.GENERIC;
    }

    default String[] singularCases() {
        return null;
    }

    default String plural() {
        return null;
    }

    default String[] pluralCases() {
        return null;
    }
}

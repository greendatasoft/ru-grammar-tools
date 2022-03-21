package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;

/**
 * Represents a word (noun, adjective) details.
 * A part of {@link Phrase}.
 */
public interface Word {
    Gender gender();

    Boolean animate();

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

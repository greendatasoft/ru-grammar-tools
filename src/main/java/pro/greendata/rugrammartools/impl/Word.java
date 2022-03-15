package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;

/**
 * Represents a word details.
 * A part of {@link Phrase}.
 */
public interface Word {
    Gender gender();

    Boolean animate();

    boolean isIndeclinable();
}

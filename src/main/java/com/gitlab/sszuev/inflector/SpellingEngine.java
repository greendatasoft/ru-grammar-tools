package com.gitlab.sszuev.inflector;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by @ssz on 18.02.2022.
 */
public interface SpellingEngine {

    /**
     * Spells the given {@code number}, e.g. {@code 25101 -> "двадцать пять тысяч сто один"}.
     *
     * @param number {@link BigDecimal}, not {@code null}
     * @return the corresponding {@code String}
     */
    String spell(BigDecimal number);

    /**
     * Spells the given ordinal {@code number}, e.g. {@code 25101 -> "двадцать пять тысяч сто первая"}.
     *
     * @param number {@link BigInteger}, not {@code null}
     * @param gender {@link Gender}
     * @return the corresponding {@code String}
     */
    String spellOrdinal(BigInteger number, Gender gender);

    default String spell(long number) {
        return spell(BigDecimal.valueOf(number));
    }

    default String spell(double number) {
        return spell(BigDecimal.valueOf(number));
    }

    default String spellOrdinal(long number, Gender gender) {
        if (number < 0) throw new IllegalArgumentException("Negative ordinal number:" + number);
        return spellOrdinal(BigInteger.valueOf(number), gender);
    }
}

package com.gitlab.sszuev.inflector;

import java.math.BigDecimal;

/**
 * Created by @ssz on 18.02.2022.
 */
public interface SpellingEngine {

    /**
     * Spells the given {@code number}, e.g. {@code 25101 -> "двадцать пять тысяч сто один"}.
     *
     * @param number {@link BigDecimal}, not {@code null}
     * @return corresponding {@code String}
     */
    String spell(BigDecimal number);

    default String spell(long number) {
        return spell(BigDecimal.valueOf(number));
    }

    default String spell(double number) {
        return spell(BigDecimal.valueOf(number));
    }
}

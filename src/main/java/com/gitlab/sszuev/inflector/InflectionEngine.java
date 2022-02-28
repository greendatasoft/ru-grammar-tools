package com.gitlab.sszuev.inflector;

import java.util.Objects;

/**
 * An engine for running inflection process.
 * Created by @ssz on 27.11.2020.
 */
public interface InflectionEngine {
    /**
     * Declines the given {@code word} in accordance with the specified settings.
     * This is the generic method.
     *
     * @param word       {@code String}, single word or phrase, a term, in nominative case, not {@code null}
     * @param type       {@link WordType}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender}, russian feminine, masculine or neuter,
     *                   {@code null} to choose automatically (usually it is {@link Gender#MALE}) or for undefined cases
     * @param animated   {@code Boolean} can be specified if {@code type = } {@link WordType#GENERIC_NOUN},
     *                   {@code null} for default behaviour
     * @param plural     {@code Boolean} if {@code true} then plural,
     *                   {@code false} for singular or {@code null} for default behaviour
     * @return {@code String}
     */
    String inflect(String word, WordType type, Case declension, Gender gender, Boolean animated, Boolean plural);

    /**
     * Declines the given {@code phrase} (combination of words: job-title, organization name)
     * into the specified declension case.
     *
     * @param phrase     {@code String} not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param animated   {@code Boolean} the names of organizations are usually inanimate, the names of professions are animate
     * @return {@code String} -  a phrase in the selected case
     */
    default String inflectRegularTerm(String phrase, Case declension, Boolean animated) {
        // this is the default rule, which works only for several simplest cases
        return inflect(phrase, WordType.GENERIC_NOUN, declension, Gender.MALE, animated, false);
    }

    /**
     * Declines the given {@code number} with the {@code unit} into the specified declension case.
     *
     * @param number     {@code String} not {@code null}, a numeral in singular nominative case, e.g.{@code "пять"}
     * @param unit       {@code String} not {@code null}, a noun in singular nominative case, e.g.{@code "заяц"}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a phrase in the selected case
     * @see SpellingEngine#spell(java.math.BigDecimal)
     */
    default String inflectNumeral(String number, String unit, Case declension) {
        // this is the default rule, it works only for numbers with fractions
        String word = Objects.requireNonNull(number) + " " + inflect(Objects.requireNonNull(unit),
                WordType.GENERIC_NOUN, Case.GENITIVE, Gender.MALE, null, null);
        return inflectNumeral(word, declension);
    }

    /**
     * Declines the given {@code number} (i.e. inanimate numeral) into the specified declension case.
     *
     * @param number     {@code String} not {@code null}, e.g.{@code "пятьсот"}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a numeral phrase in the selected case
     * @see SpellingEngine#spell(java.math.BigDecimal)
     */
    default String inflectNumeral(String number, Case declension) {
        // this is default rule, which works only for several cases
        return inflect(number, WordType.NUMERALS, declension, null, null, null);
    }
}

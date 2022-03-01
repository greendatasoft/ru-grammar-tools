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

    /**
     * Declines the given {@code sfp} (full name) into the specified declension case.
     *
     * @param sfp        {@code String} - surname+firstname+patronymic with space as separator, surname is mandatory
     * @param declension {@link Case declension case}, not {@code null}
     * @return surname+firstname+patronymic in desired declension case
     */
    default String inflectFullName(String sfp, Case declension) {
        return String.join(" ", inflectFullName(sfp.split("\\s+"), declension, null));
    }

    /**
     * Declines the given {@code sfp} (full name) into the specified declension case.
     *
     * @param sfp        an {@code Array} with full name: either {@code [surname]} (e.g. {@code "Петров"}),
     *                   or {@code [surname, firstname]} (e.g. {@code "Петров Петр"}),
     *                   or {@code [surname, firstname, patronymic]} (e.g. {@code "Петров Петр Петрович"})
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender} or {@code null} to guess
     * @return {@code Array} with full name in desired declension case
     */
    default String[] inflectFullName(String[] sfp, Case declension, Gender gender) {
        if (sfp.length > 3 || sfp.length == 0) {
            throw new IllegalArgumentException();
        }
        String s = inflect(sfp[0], WordType.FAMILY_NAME, declension, gender, true, false);
        if (sfp.length == 1) {
            return new String[]{s};
        }
        String f = inflect(sfp[1], WordType.FIRST_NAME, declension, gender, true, false);
        if (sfp.length == 2) {
            return new String[]{s, f};
        }
        String p = inflect(sfp[2], WordType.PATRONYMIC_NAME, declension, gender, true, false);
        return new String[]{s, f, p};
    }
}

package com.gitlab.sszuev.inflector;

/**
 * An engine for running inflection process.
 * Created by @ssz on 27.11.2020.
 */
public interface InflectorEngine {
    /**
     * Declines the given {@code word} in accordance with the specified settings.
     * Good for Russian full-name (firstname patronymic surname, FPS).
     *
     * @param word       {@code String}, single word or phrase, a term, not {@code null}
     * @param type       {@link WordType}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender}, russian feminine, masculine or neuter,
     *                   {@code null} to choose automatically (usually it is {@link Gender#MALE})
     * @param plural     {@code boolean} if {@code true} then plural, otherwise singular
     * @return {@code String}
     */
    String inflect(String word, WordType type, Case declension, Gender gender, boolean plural);

    /**
     * Declines the given {@code word} into the specified declension case.
     * Good for declension of job-titles and professions.
     *
     * @param word       {@code String} not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a phrase in the selected case
     */
    default String inflectRegularTerm(String word, Case declension) {
        return inflect(word, WordType.REGULAR_TERM, declension, Gender.MALE, false);
    }

    /**
     * Declines the given {@code number} (i.e. inanimate numeral) into the specified declension case.
     *
     * @param number     {@code String} not {@code null}, e.g.{@code "пятьсот"}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a numeral phrase in the selected case
     */
    default String inflectNumeral(String number, Case declension) {
        return inflect(number, WordType.NUMERALS, declension, Gender.NEUTER, false);
    }
}

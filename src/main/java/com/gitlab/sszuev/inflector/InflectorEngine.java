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
     * @param word       {@code String} not {@code null}
     * @param type       {@link WordType} (part of FPS or profession), not {@code null}
     * @param gender     {@link Gender}, russian feminine, masculine or neuter, by default {@link Gender#MALE}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String}
     */
    String inflect(String word, WordType type, Gender gender, Case declension);

    /**
     * Declines the given {@code word} into the specified declension case.
     * Good for declension of job-titles and professions.
     *
     * @param word       {@code String} not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a phrase in the selected case
     */
    default String inflect(String word, Case declension) {
        return inflect(word, WordType.REGULAR_TERM, Gender.MALE, declension);
    }

    /**
     * Declines the given {@code number} into the specified declension case.
     *
     * @param number     {@code String} not {@code null}, e.g.{@code "пятьсот"}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a numeral phrase in the selected case
     */
    default String inflectNumeral(String number, Case declension) {
        return inflect(number, WordType.NUMERALS, Gender.NEUTER, declension);
    }
}

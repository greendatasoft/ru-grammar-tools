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
     * @param gender     {@link Gender}, russian feminine, masculine or neuter, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String}
     */
    String inflect(String word, WordType type, Gender gender, Case declension);

    /**
     * Declines the given {@code word} in the specified declension case.
     * Good for declension of job-titles and professions.
     *
     * @param word       {@code String} not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a phrase in the selected case
     */
    default String inflect(String word, Case declension) {
        return inflect(word, WordType.REGULAR_TERM, Gender.MALE, declension);
    }
}

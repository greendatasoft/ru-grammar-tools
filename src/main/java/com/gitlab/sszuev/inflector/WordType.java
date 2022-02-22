package com.gitlab.sszuev.inflector;

/**
 * The word type.
 * Created by @ssz on 27.11.2020.
 */
public enum WordType {
    FIRST_NAME, // e.g. 'Петр'
    PATRONYMIC_NAME, // e.g. 'Петрович'
    FAMILY_NAME, // e.g. 'Петров'
    NUMERALS, // e.g. 'пятьсот'
    GENERIC_NOUN, // a singular noun in nominative case
}

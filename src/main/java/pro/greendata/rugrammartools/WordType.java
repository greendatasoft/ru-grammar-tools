package pro.greendata.rugrammartools;

/**
 * The word type.
 * Created by @ssz on 27.11.2020.
 */
public enum WordType {
    FIRST_NAME, // e.g. 'Петр'
    PATRONYMIC_NAME, // e.g. 'Петрович'
    FAMILY_NAME, // e.g. 'Петров'
    NUMERALS, // a cardinal numeral, e.g. 'пятьсот'
    GENERIC, // a regular singular noun or adjective in nominative case
}

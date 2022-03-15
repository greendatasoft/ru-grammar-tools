package pro.greendata.rugrammartools.impl;

/**
 * The petrovich rule types.
 * <p>
 * Created by @ssz on 27.11.2020.
 */
public enum RuleType {
    FIRST_NAME, // e.g. 'Петр'
    PATRONYMIC_NAME, // e.g. 'Петрович'
    FAMILY_NAME, // e.g. 'Петров'
    NUMERAL, // a cardinal numeral, e.g. 'пятьсот'
    GENERIC, // a regular singular noun or adjective in nominative case
}

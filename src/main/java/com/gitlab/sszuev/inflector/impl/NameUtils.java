package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.Gender;

import java.util.List;

/**
 * Created by @ssz on 28.02.2022.
 */
public class NameUtils {
    private static final List<String> FEMALE_PATRONYMIC_ENDINGS = List.of("овна", "евна", "ична");
    private static final List<String> MALE_PATRONYMIC_ENDINGS = List.of("ович", "евич", "ич");
    private static final List<String> FEMALE_SURNAME_ENDINGS = List.of("ова", "ева", "ина", "ая", "яя", "екая", "цкая");
    private static final List<String> MALE_SURNAME_ENDINGS = List.of("ов", "ев", "ин", "ын", "ой", "цкий", "ский", "цкой", "ской", "ый");

    public static Gender guessGenderByFirstName(String name) {
        if (isFemaleFirstName(name)) {
            return Gender.FEMALE;
        }
        if (isMaleFirstName(name)) {
            return Gender.MALE;
        }
        return null;
    }

    public static Gender guessGenderByPatronymicName(String name) {
        if (canBeFemalePatronymic(name)) {
            return Gender.FEMALE;
        }
        if (canBeMalePatronymic(name)) {
            return Gender.MALE;
        }
        return null;
    }

    public static Gender guessGenderBySurname(String name) {
        if (canBeFemaleSurname(name)) {
            return Gender.FEMALE;
        }
        if (canBeMaleSurname(name)) {
            return Gender.MALE;
        }
        return null;
    }

    public static boolean isFemaleFirstName(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return PlainDictionary.FEMALE_NAMES.contains(nw);
    }

    public static boolean isMaleFirstName(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return PlainDictionary.MALE_NAMES.contains(nw);
    }

    public static boolean canBeFemalePatronymic(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return FEMALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeMalePatronymic(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return MALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeFemaleSurname(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return FEMALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeMaleSurname(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return MALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith);
    }
}

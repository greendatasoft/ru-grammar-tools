package pro.greendata.rugrammartools.impl.utils;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.dictionaries.PlainDictionary;

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
        if (isFemaleFirstname(name)) {
            return Gender.FEMALE;
        }
        if (isMaleFirstname(name)) {
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

    public static boolean isFirstname(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return PlainDictionary.FEMALE_NAMES.contains(nw) || PlainDictionary.MALE_NAMES.contains(nw);
    }

    public static boolean isFemaleFirstname(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return PlainDictionary.FEMALE_NAMES.contains(nw);
    }

    public static boolean isMaleFirstname(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return PlainDictionary.MALE_NAMES.contains(nw);
    }

    public static boolean canBePatronymic(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return FEMALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith) || MALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeFemalePatronymic(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return FEMALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeMalePatronymic(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return MALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeSurname(String word) {
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        return FEMALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith) || MALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith);
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

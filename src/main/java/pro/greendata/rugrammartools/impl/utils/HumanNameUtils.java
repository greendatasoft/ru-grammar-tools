package pro.greendata.rugrammartools.impl.utils;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.PlainDictionary;

import java.util.List;

/**
 * Utilities to work with human names.
 * Created by @ssz on 28.02.2022.
 *
 * @see GrammarUtils
 * @see NumeralUtils
 */
public class HumanNameUtils {
    private static final String INITIALS_LETTERS_IN_LOWERCASE = "абвгдеёжзийклмнопрстуфхцчшщыэюя";
    private static final String INITIALS_LETTERS_IN_UPPERCASE = "абвгдеёжзийклмнопрстуфхцчшщыэюя".toUpperCase(TextUtils.DEFAULT_LOCALE);
    private static final String INITIALS_PATTERN = ("[" + INITIALS_LETTERS_IN_LOWERCASE + INITIALS_LETTERS_IN_UPPERCASE + "]\\.").repeat(2);

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
        String nw = TextUtils.normalize(word);
        return PlainDictionary.FEMALE_NAMES.contains(nw) || PlainDictionary.MALE_NAMES.contains(nw);
    }

    public static boolean isFemaleFirstname(String word) {
        String nw = TextUtils.normalize(word);
        return PlainDictionary.FEMALE_NAMES.contains(nw);
    }

    public static boolean isMaleFirstname(String word) {
        String nw = TextUtils.normalize(word);
        return PlainDictionary.MALE_NAMES.contains(nw);
    }

    public static boolean canBePatronymic(String word) {
        String nw = TextUtils.normalize(word);
        return FEMALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith) || MALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeFemalePatronymic(String word) {
        String nw = TextUtils.normalize(word);
        return FEMALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeMalePatronymic(String word) {
        String nw = TextUtils.normalize(word);
        return MALE_PATRONYMIC_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeSurname(String word) {
        String nw = TextUtils.normalize(word);
        return FEMALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith) || MALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeFemaleSurname(String word) {
        String nw = TextUtils.normalize(word);
        return FEMALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeMaleSurname(String word) {
        String nw = TextUtils.normalize(word);
        return MALE_SURNAME_ENDINGS.stream().anyMatch(nw::endsWith);
    }

    public static boolean canBeInitials(String word) {
        return word.matches(INITIALS_PATTERN);
    }

    /**
     * Guesses gender by SFP.
     *
     * @param sfp an {@code Array} with full name: either {@code [surname]} (e.g. {@code "Петров"}),
     *            or {@code [surname, firstname]} (e.g. {@code "Петров Петр"}),
     *            or {@code [surname, firstname, patronymic]} (e.g. {@code "Петров Петр Петрович"})
     * @return {@link Gender} or {@code null}
     */
    public static Gender guessGenderByFullName(String[] sfp) {
        // by first name
        Gender g;
        if (sfp.length > 1) {
            g = guessGenderByFirstName(sfp[1]);
            if (g != null) {
                return g;
            }
        }
        // by patronymic
        if (sfp.length > 2) {
            g = guessGenderByPatronymicName(sfp[2]);
            if (g != null) {
                return g;
            }
        }
        // family
        return guessGenderBySurname(sfp[0]);
    }
}

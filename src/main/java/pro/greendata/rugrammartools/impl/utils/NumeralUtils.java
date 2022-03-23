package pro.greendata.rugrammartools.impl.utils;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.dictionaries.PlainDictionary;

import java.util.List;
import java.util.Set;

/**
 * Utilities to work with numerals.
 * @see GrammarUtils
 * @see HumanNameUtils
 */
public class NumeralUtils {

    static final Set<String> FEMALE_NUMERALS = Set.of("одна", "две", "тысяча", "тысяч", "тысячи", "целая");
    static final Set<String> MALE_NUMERALS = Set.of("один");
    static final List<String> ORDINAL_NUMERAL_ENDINGS = List.of("ой", "ый", "ий", "ая", "ое");

    /**
     * Determines whether the given {@code word} can be an ordinal numeral.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeOrdinalNumeral(String word) {
        return TextUtils.endsWithOneOfIgnoreCase(word, ORDINAL_NUMERAL_ENDINGS) && !canBeFraction(word);
    }

    /**
     * Determines whether the given {@code phrase} can be fraction numeral.
     *
     * @param phrase {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFraction(String phrase) {
        phrase = TextUtils.normalize(phrase, Dictionary.LOCALE);
        return phrase.contains(" целых ") || phrase.contains("одна целая ");
    }

    /**
     * Answers {@code true} if the given {@code phrase} contains big numeral markers.
     *
     * @param phrase {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean containsBigCardinalNumeral(String phrase) {
        String nw = TextUtils.normalize(phrase, Dictionary.LOCALE);
        if ("тысячи".equals(nw) || "тысяч".equals(nw)) {
            // ноль целых две десятых, десять тысяч целых тридцать три сотых
            return true;
        }
        for (String s : PlainDictionary.BIG_CARDINAL_NUMERALS) {
            // два миллиона целых две десятых, миллион целых одна сотая
            if (nw.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the gender of the specified simple (i.e. not composite) numeral.
     *
     * @param word {@code String}, not {@code null}
     * @return {@link Gender}
     */
    public static Gender guessGenderOfSingleNumeral(String word) {
        String nw = TextUtils.normalize(word, Dictionary.LOCALE);
        if (MALE_NUMERALS.contains(nw) || nw.endsWith("ый")) { // один, десятый
            return Gender.MALE;
        }
        if (FEMALE_NUMERALS.contains(nw) || nw.endsWith("ая")) { // одна, десятая, сотая
            return Gender.FEMALE;
        }
        return Gender.NEUTER;
    }

    /**
     * Changes the gender of the specified cardinal numeral word.
     *
     * @param numeral {@code String}, a cardinal numeral in nominative case, not {@code null}
     * @param gender  {@link Gender}, not {@code null}
     * @return {@code String}
     */
    public static String changeGenderOfCardinalNumeral(String numeral, Gender gender) {
        String nw = TextUtils.normalize(numeral, Dictionary.LOCALE);
        switch (nw) {
            case "один":
                return TextUtils.toProperCase(numeral, GrammarUtils.select("одна", "одно", "один", gender));
            case "два":
                return TextUtils.toProperCase(numeral, GrammarUtils.select("две", "два", "два", gender));
        }
        return numeral;
    }

    /**
     * Changes the gender of the specified ordinal numeral word.
     *
     * @param numeral {@code String}, an ordinal numeral in nominative case, not {@code null}
     * @param gender  {@link Gender}, not {@code null}
     * @return {@code String}
     */
    public static String changeGenderOfOrdinalNumeral(String numeral, Gender gender) {
        String nw = TextUtils.normalize(numeral, Dictionary.LOCALE);
        if ("третий".equals(nw)) {
            return TextUtils.toProperCase(numeral, GrammarUtils.select("третья", "третье", "третий", gender));
        }
        // одиннадцатый одиннадцатое одиннадцатая
        // четвёртый четвёртое четвёртая
        // седьмой седьмое седьмая
        String ending = GrammarUtils.select("ая", "ое", nw.endsWith("ый") ? "ый" : "ой", gender);
        String res = TextUtils.replaceEnd(nw, 2, ending, Dictionary.LOCALE);
        return TextUtils.toProperCase(numeral, res);
    }

    /**
     * Answers {@code true} if the given number is {@code ноль}.
     *
     * @param word {@code String}
     * @return {@code boolean}
     */
    public static boolean isZero(String word) {
        return "ноль".equals(TextUtils.normalize(word, Dictionary.LOCALE));
    }

    /**
     * Answers {@code true} if the given {@code phrase} ends with {@code один}.
     *
     * @param phrase {@code String}
     * @return {@code boolean}
     */
    public static boolean endsWithCardinalOne(String phrase) {
        return GrammarUtils.endsWithWord(TextUtils.normalize(phrase, Dictionary.LOCALE), "один");
    }

    /**
     * Answers {@code true} if the given {@code phrase} ends with {@code два, три, четыре}.
     *
     * @param phrase {@code String}
     * @return {@code boolean}
     */
    public static boolean endsWithCardinalTwoThreeFour(String phrase) {
        String nw = TextUtils.normalize(phrase, Dictionary.LOCALE);
        return GrammarUtils.endsWithWord(nw, "два") ||
                GrammarUtils.endsWithWord(nw, "три") || GrammarUtils.endsWithWord(nw, "четыре");
    }
}

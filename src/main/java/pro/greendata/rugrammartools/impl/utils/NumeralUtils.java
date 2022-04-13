package pro.greendata.rugrammartools.impl.utils;

import pro.greendata.rugrammartools.Gender;
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
    static final Set<String> ORDINAL_NUMERAL_DISCHARGE_ENDING = Set.of("десятый", "сотый", "тысячный", "миллионный",
            "миллиардный", "триллионный");
    static final List<String> ORDINAL_NUMERAL_ENDINGS = List.of("первый", "второй", "третий", "четвёртый", "пятый", "шестой", "седьмой",
            "восьмой", "девятый", "десятый", "дцатый", "сороковой", "сотый", "тысячный");
    static final List<String> CARDINAL_NUMERAL_ENDINGS = List.of("один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять",
            "десять", "дцать", "сорок", "десят", "сто", "сти", "ста", "сот");

    //TODO: FIX
    private static final Set<String> NOT_NUMERAL = Set.of("место", "места", "двери");

    /**
     * Determines whether the given {@code word} can be a numeral.
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    //TODO: should do it right
    public static boolean canBeNumeral(String word) {
        String ordinalWord = changeGenderOfOrdinalDischargeNumeral(word);
        word = TextUtils.normalize(word);
        return !NOT_NUMERAL.contains(word) &&
                (
                        canBeCardinal(word) ||
                        canBeOrdinalNumeral(word) ||
                        TextUtils.endsWithOneOfIgnoreCase(ordinalWord, ORDINAL_NUMERAL_DISCHARGE_ENDING)
                );
    }

    /**
     * Determines whether the given {@code word} can be a cardinal numeral.
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeCardinal(String word) {
        return (TextUtils.endsWithOneOfIgnoreCase(word, CARDINAL_NUMERAL_ENDINGS) ||
                TextUtils.startsWithOneOfIgnoreCase(word, FEMALE_NUMERALS) ||
                containsBigCardinalNumeral(word) ||
                FEMALE_NUMERALS.contains(word));
    }

    /**
     * Determines whether the given {@code word} can be an ordinal numeral.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeOrdinalNumeral(String word) {
        String[] parts = word.split(" ");
        word = parts.length > 1 ? changeGenderOfOrdinalNumeral(parts[parts.length - 1], Gender.MALE) :
                changeGenderOfOrdinalNumeral(word, Gender.MALE);
        return (TextUtils.endsWithOneOfIgnoreCase(word, ORDINAL_NUMERAL_ENDINGS) ||
                TextUtils.endsWithOneOfIgnoreCase(word, PlainDictionary.BIG_ORDINAL_NUMERALS)) &&
                !canBeFraction(word);
    }

    /**
     * Determines whether the given {@code phrase} can be fraction numeral.
     *
     * @param phrase {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFraction(String phrase) {
        phrase = TextUtils.normalize(phrase);
        return phrase.contains(" целых ") || phrase.contains("одна целая ");
    }

    /**
     * Answers {@code true} if the given {@code phrase} contains big numeral markers.
     *
     * @param phrase {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean containsBigCardinalNumeral(String phrase) {
        String nw = TextUtils.normalize(phrase);
        if ("тысячи".equals(nw) || "тысяч".equals(nw)) {
            // ноль целых две десятых, десять тысяч целых тридцать три сотых
            return true;
        }
        for (String s : PlainDictionary.BIG_CARDINAL_NUMERALS) {
            // два миллиона целых две десятых, миллион целых одна сотая
            if (nw.startsWith(s) && !nw.equals(s + "ы")) {
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
        String nw = TextUtils.normalize(word);
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
        String nw = TextUtils.normalize(numeral);
        switch (nw) {
            case "один":
                return TextUtils.toProperCase(numeral, GrammarUtils.select("одна", "одно", "один", gender));
            case "два":
                return TextUtils.toProperCase(numeral, GrammarUtils.select("две", "два", "два", gender));
        }
        return numeral;
    }

    //TODO: temporary solution
    private static String changeGenderOfOrdinalDischargeNumeral(String numeral) {
        if (numeral.length() < 2) {
            return numeral;
        }
        String nw = TextUtils.normalize(numeral);
        String ending = GrammarUtils.select("ая", "ое", "ый", Gender.MALE);
        String res = TextUtils.replaceEnd(nw, 2, ending);
        return TextUtils.toProperCase(numeral, res);
    }

    /**
     * Changes the gender of the specified ordinal numeral word.
     *
     * @param numeral {@code String}, an ordinal numeral in nominative case, not {@code null}
     * @param gender  {@link Gender}, not {@code null}
     * @return {@code String}
     */
    public static String changeGenderOfOrdinalNumeral(String numeral, Gender gender) {
        if (numeral.length() < 2) {
            return numeral;
        }
        String nw = TextUtils.normalize(numeral);
        if ("третий".equals(nw)) {
            return TextUtils.toProperCase(numeral, GrammarUtils.select("третья", "третье", "третий", gender));
        }
        // одиннадцатый одиннадцатое одиннадцатая
        // четвёртый четвёртое четвёртая
        // седьмой седьмое седьмая
        String ending = GrammarUtils.select("ая", "ое", nw.endsWith("ый") ? "ый" : "ой", gender);
        String res = TextUtils.replaceEnd(nw, 2, ending);
        return TextUtils.toProperCase(numeral, res);
    }

    /**
     * Answers {@code true} if the given number is {@code ноль}.
     *
     * @param word {@code String}
     * @return {@code boolean}
     */
    public static boolean isZero(String word) {
        return "ноль".equals(TextUtils.normalize(word));
    }

    /**
     * Answers {@code true} if the given {@code phrase} ends with {@code один}.
     *
     * @param phrase {@code String}
     * @return {@code boolean}
     */
    public static boolean endsWithCardinalOne(String phrase) {
        return GrammarUtils.endsWithWord(TextUtils.normalize(phrase), "один");
    }

    /**
     * Answers {@code true} if the given {@code phrase} ends with {@code два, три, четыре}.
     *
     * @param phrase {@code String}
     * @return {@code boolean}
     */
    public static boolean endsWithCardinalTwoThreeFour(String phrase) {
        String nw = TextUtils.normalize(phrase);
        return GrammarUtils.endsWithWord(nw, "два") ||
                GrammarUtils.endsWithWord(nw, "три") || GrammarUtils.endsWithWord(nw, "четыре");
    }
}

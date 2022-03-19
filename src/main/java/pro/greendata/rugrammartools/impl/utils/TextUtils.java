package pro.greendata.rugrammartools.impl.utils;

import java.util.Collection;
import java.util.Locale;

/**
 * Created by @ssz on 25.02.2022.
 */
public class TextUtils {

    /**
     * Makes a normalized string (lowercase without trailing spaces).
     *
     * @param orig   {@code String}, not {@code null}
     * @param locale {@link Locale} use the case transformation rules for this locale
     * @return {@code String}
     */
    public static String normalize(String orig, Locale locale) {
        return orig.trim().toLowerCase(locale);
    }

    /**
     * Appends ending to the original string preserving case.
     *
     * @param orig   {@code String}, not {@code null}
     * @param ending {@code String} to append
     * @param locale {@link Locale} use the case transformation rules for this locale
     * @return {@code String}
     */
    public static String appendEnd(String orig, String ending, Locale locale) {
        return orig + toUpperCaseIfNeeded(orig, ending, locale);
    }

    /**
     * Replaces the ending of the original string with the given one preserving case.
     *
     * @param orig         {@code String}, not {@code null}
     * @param numberToTrim {@code int} - the number of symbols to cut from the end of {@code orig}
     * @param ending       {@code String}
     * @param locale       {@link Locale} use the case transformation rules for this locale
     * @return {@code String}
     */
    public static String replaceEnd(String orig, int numberToTrim, String ending, Locale locale) {
        if (isUpperCase(orig, orig.length() - numberToTrim, orig.length())) {
            ending = ending.toUpperCase(locale);
        }
        return orig.substring(0, orig.length() - numberToTrim) + ending;
    }

    /**
     * Answers {@code true} if the string has an uppercase substring specified by indexes.
     *
     * @param string     {@code String}, not {@code null}
     * @param beginIndex the beginning index, inclusive
     * @param endIndex   the ending index, exclusive
     * @return {@code boolean}
     */
    public static boolean isUpperCase(String string, int beginIndex, int endIndex) {
        return string.substring(beginIndex, endIndex).chars().allMatch(Character::isUpperCase);
    }

    /**
     * Answers {@code true} if the string is in uppercase.
     *
     * @param string {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean isUpperCase(String string) {
        return string.chars().allMatch(Character::isUpperCase);
    }

    /**
     * Answers {@code true} if the string has upper-cased and lower-cased characters.
     *
     * @param string {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean isMixedCase(String string) {
        long upper = string.chars().filter(Character::isUpperCase).count();
        return upper > 0 && string.length() != upper;
    }

    /**
     * Makes a new upper-cased {@code string} from the {@code original} if it is in upper-case,
     * otherwise returns {@code string} unchanged.
     *
     * @param original {@code String}, not {@code null}
     * @param string   {@code String}, not {@code null}
     * @param locale   {@link Locale} use the case transformation rules for this locale
     * @return {@code String}
     */
    public static String toUpperCaseIfNeeded(String original, String string, Locale locale) {
        return isUpperCase(original) ? string.toUpperCase(locale) : string;
    }

    /**
     * Formats the given {@code string} according to the {@code template} case.
     *
     * @param template {@code String}
     * @param string   {@code String}
     * @return {@code String}
     */
    public static String toProperCase(String template, String string) {
        StringBuilder res = new StringBuilder();
        char[] origChars = template.toCharArray();
        char[] resChars = string.toCharArray();
        int i = 0;
        boolean isUpperCaseEnding = false;
        for (; i < Math.min(origChars.length, resChars.length); i++) {
            char origChar = origChars[i];
            char resChar = resChars[i];
            if (equalsIgnoreCase(origChar, resChar)) {
                res.append(origChar);
            } else {
                isUpperCaseEnding = Character.isUpperCase(origChar);
                break;
            }
        }
        for (; i < resChars.length; i++) {
            char resChar = resChars[i];
            if (isUpperCaseEnding) {
                resChar = Character.toUpperCase(resChar);
            } else {
                resChar = Character.toLowerCase(resChar);
            }
            res.append(resChar);
        }
        return res.toString();
    }

    /**
     * Answers {@code true} if two characters are equal ignoring case.
     *
     * @param a {@code char}
     * @param b {@code char}
     * @return {@code boolean}
     */
    public static boolean equalsIgnoreCase(char a, char b) {
        return a == b || Character.toUpperCase(a) == Character.toUpperCase(b);
    }

    /**
     * Answers {@code true} if the given {@code string} ends with the given {@code ending} ignoring case.
     *
     * @param string {@code String}
     * @param ending {@code String}
     * @return {@code boolean}
     */
    public static boolean endsWithIgnoreCase(String string, String ending) {
        int index = string.length() - ending.length();
        if (index < 0) {
            return false;
        }
        return string.substring(index).equalsIgnoreCase(ending);
    }

    /**
     * Answers {@code true} if the given {@code string} ends with on of the ending from the specified collection, ignoring case.
     *
     * @param string  {@code String}
     * @param endings {@code Collection} of {@code String}s
     * @return {@code boolean}
     */
    public static boolean endsWithOneOfIgnoreCase(String string, Collection<String> endings) {
        for (String ending : endings) {
            if (endsWithIgnoreCase(string, ending)) {
                return true;
            }
        }
        return false;
    }
}

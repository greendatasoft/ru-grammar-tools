package pro.greendata.rugrammartools.impl.utils;

import java.util.Collection;
import java.util.Locale;

/**
 * Created by @ssz on 25.02.2022.
 */
public class MiscStringUtils {

    public static String normalize(String s, Locale locale) {
        return s.trim().toLowerCase(locale);
    }

    public static String appendEnd(String orig, String ending, Locale locale) {
        return orig + toUpperCaseIfNeeded(orig, ending, locale);
    }

    public static String replaceEnd(String orig, int numberToTrim, String replacement, Locale locale) {
        if (isUpperCase(orig, orig.length() - numberToTrim, orig.length())) {
            replacement = replacement.toUpperCase(locale);
        }
        return orig.substring(0, orig.length() - numberToTrim) + replacement;
    }

    public static boolean isUpperCase(String s, int start, int end) {
        return s.substring(start, end).chars().allMatch(Character::isUpperCase);
    }

    public static boolean isUpperCase(String s) {
        return s.chars().allMatch(Character::isUpperCase);
    }

    public static String toUpperCaseIfNeeded(String origin, String res, Locale locale) {
        return isUpperCase(origin) ? res.toUpperCase(locale) : res;
    }

    /**
     * Formats the given {@code string} according {@code template} case.
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
        boolean isUpperCase = false;
        for (; i < Math.min(origChars.length, resChars.length); i++) {
            char origChar = origChars[i];
            char resChar = resChars[i];
            if (equalsIgnoreCase(origChar, resChar)) {
                res.append(origChar);
            } else {
                isUpperCase = Character.isUpperCase(origChar);
                break;
            }
        }
        for (; i < resChars.length; i++) {
            char resChar = resChars[i];
            if (isUpperCase) {
                resChar = Character.toUpperCase(resChar);
            }
            res.append(resChar);
        }
        return res.toString();
    }

    public static boolean equalsIgnoreCase(char a, char b) {
        return a == b || Character.toUpperCase(a) == Character.toUpperCase(b);
    }

    public static boolean endsWithIgnoreCase(String string, String ending) {
        int index = string.length() - ending.length();
        if (index < 0) {
            return false;
        }
        return string.substring(index).equalsIgnoreCase(ending);
    }

    public static boolean endsWithOneOfIgnoreCase(String word, Collection<String> endings) {
        for (String ending : endings) {
            if (endsWithIgnoreCase(word, ending)) {
                return true;
            }
        }
        return false;
    }
}

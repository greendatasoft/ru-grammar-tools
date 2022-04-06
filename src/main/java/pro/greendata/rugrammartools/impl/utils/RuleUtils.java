package pro.greendata.rugrammartools.impl.utils;

public class RuleUtils {
    public static final char KEEP_CHARACTER = '.';
    public static final String KEEP_MOD = Character.toString(KEEP_CHARACTER);
    public static final char REMOVE_CHARACTER = '-';

    /**
     * Changes the ending of the given rule according to petrovich rules.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code "xxx" + "--z" = "xz"}</li>
     * <li>{@code "xxx" + "z" = "xxxz"}</li>
     * <li>{@code "xxx" + "." = "xxx"}</li>
     * </ul>
     *
     * @param word   {@code String} a word to modify
     * @param ending {@code String}, e.g. {@code --xx}, {@code .}
     * @return {@code String}
     */
    public static String changeEnding(String word, String ending) {
        if (KEEP_MOD.equals(ending)) {
            return word;
        }
        if (ending.lastIndexOf(REMOVE_CHARACTER) < 0) {
            return word + ending;
        }
        String res = word;
        for (int i = 0; i < ending.length(); i++) {
            if (ending.charAt(i) == REMOVE_CHARACTER) {
                res = res.substring(0, res.length() - 1);
            } else {
                res += ending.substring(i);
                break;
            }
        }
        return res;
    }

    /**
     * Calculates an ending mod for the given primary and secondary words.
     *
     * @param primary   {@code String}, not {@code null}
     * @param secondary {@code String}, not {@code null}
     * @return {@code String}
     */
    public static String calcEnding(String primary, String secondary) {
        //noinspection StringEquality
        if (primary == secondary) {
            return KEEP_MOD;
        }
        //Remove only parentheses
        primary = primary.replaceAll("[()]", "");
        secondary = secondary.replaceAll("[()]", "");
        char[] first = primary.toCharArray();
        char[] second = secondary.toCharArray();
        int i = 0;
        for (; i < Math.min(second.length, first.length); i++) {
            if (first[i] != second[i]) {
                break;
            }
        }
        int num = first.length - i;
        if (num == 0 && second.length == first.length) {
            return KEEP_MOD;
        }
        StringBuilder res = new StringBuilder();
        //noinspection StringRepeatCanBeUsed -- expected to be faster than String#repeat
        for (int j = 0; j < num; j++) {
            res.append(REMOVE_CHARACTER);
        }
        for (int j = i; j < second.length; j++) {
            res.append(second[j]);
        }
        return res.toString();
    }

}

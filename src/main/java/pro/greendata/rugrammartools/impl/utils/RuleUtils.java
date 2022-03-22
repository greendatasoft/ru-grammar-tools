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
     * @param word {@code String} a word to modify
     * @param mod  {@code String}, e.g. {@code --xx}, {@code .}
     * @return {@code String}
     */
    public static String changeEnding(String word, String mod) {
        if (KEEP_MOD.equals(mod)) {
            return word;
        }
        if (mod.lastIndexOf(REMOVE_CHARACTER) < 0) {
            return word + mod;
        }
        String res = word;
        for (int i = 0; i < mod.length(); i++) {
            if (mod.charAt(i) == REMOVE_CHARACTER) {
                res = res.substring(0, res.length() - 1);
            } else {
                res += mod.substring(i);
                break;
            }
        }
        return res;
    }
}

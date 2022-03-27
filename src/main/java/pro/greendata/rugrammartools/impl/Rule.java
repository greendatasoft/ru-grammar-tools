package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Case;
import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.utils.RuleUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by @ssz on 04.12.2020.
 */
public class Rule {
    protected final Gender gender;
    protected final PartOfSpeech partOfSpeech;
    protected final Boolean plural;
    protected final Boolean animate;

    private final String[] test;
    private final String[] mods;

    public Rule(String[] test, String[] mods, Gender gender, PartOfSpeech partOfSpeech, Boolean animate, Boolean plural) {
        this.gender = Objects.requireNonNull(gender);
        this.partOfSpeech = partOfSpeech;
        this.plural = plural;
        this.animate = animate;
        this.test = Objects.requireNonNull(test);
        this.mods = Objects.requireNonNull(mods);
    }

    public String apply(Case declension, String word) {
        return RuleUtils.changeEnding(word, mode(declension));
    }

    public boolean match(String word) {
        for (String t : test) {
            if (word.endsWith(t)) {
                return true;
            }
        }
        return false;
    }

    private String mode(Case declension) {
        return mods[declension.ordinal() - 1];
    }

    public boolean matchGenderStrict(Gender gender) {
        return this.gender == gender;
    }

    public boolean matchPartOfSpeechLenient(PartOfSpeech partOfSpeech) {
        return matchLenient(this.partOfSpeech, partOfSpeech);
    }

    public boolean matchPluralLenient(Boolean plural) {
        return matchLenient(this.plural, plural);
    }

    public boolean matchAnimateLenient(Boolean animate) {
        return matchLenient(this.animate, animate);
    }

    private static <X> boolean matchLenient(X a, X b) {
        return a == null || b == null || a.equals(b);
    }

    @Override
    public String toString() {
        return String.format("Rule{gender=%s, pos=%s, plural=%s, animated=%s, test=%s, mods=%s}",
                gender, partOfSpeech, plural, animate, Arrays.toString(test), Arrays.toString(mods));
    }
}

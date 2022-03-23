package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Case;
import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.utils.RuleUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Created by @ssz on 04.12.2020.
 */
public class Rule {
    private final Gender gender;
    private final Boolean plural;
    private final Boolean animate;

    private final String[] test;
    private final String[] mods;

    public Rule(String[] test, String[] mods, Gender gender, Boolean animate, Boolean plural) {
        this.gender = Objects.requireNonNull(gender);
        this.plural = plural;
        this.animate = animate;
        this.test = Objects.requireNonNull(test);
        this.mods = Objects.requireNonNull(mods);
    }

    public String apply(Case declension, String word) {
        return RuleUtils.changeEnding(word, mode(declension));
    }

    public Stream<String> test() {
        return Arrays.stream(test);
    }

    public String mode(Case declension) {
        return mods[declension.ordinal() - 1];
    }

    public boolean matchPlural(Boolean plural) {
        return matchBoolean(plural, this.plural);
    }

    public boolean matchAnimate(Boolean animated) {
        return matchBoolean(animated, this.animate);
    }

    public boolean matchGender(Gender gender) {
        // weird logic by historical reasons
        return this.gender == Gender.NEUTER || this.gender == gender;
    }

    public boolean matchPluralStrict(Boolean plural) {
        return this.plural == plural;
    }

    public boolean matchAnimateStrict(Boolean animated) {
        return this.animate == animated;
    }

    public boolean matchGenderStrict(Gender gender) {
        return this.gender == gender;
    }

    private static Boolean matchBoolean(Boolean a, Boolean b) {
        if (a == null) {
            return true;
        }
        if (b == null) {
            return true;
        }
        return a == b;
    }

    @Override
    public String toString() {
        return String.format("Rule{gender=%s, plural=%s, animated=%s, test=%s, mods=%s}",
                gender, plural, animate, Arrays.toString(test), Arrays.toString(mods));
    }
}

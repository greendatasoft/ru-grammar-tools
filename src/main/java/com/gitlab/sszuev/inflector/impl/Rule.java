package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.Case;
import com.gitlab.sszuev.inflector.Gender;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Created by @ssz on 04.12.2020.
 */
public class Rule {
    private final Gender gender;
    private final Boolean plural;
    private final Boolean animated;

    private final String[] test;
    private final String[] mods;

    public Rule(String[] test, String[] mods, Gender gender, Boolean animated, Boolean plural) {
        this.gender = Objects.requireNonNull(gender);
        this.plural = plural;
        this.animated = animated;
        this.test = Objects.requireNonNull(test);
        this.mods = Objects.requireNonNull(mods);
    }

    public String apply(Case declension, String word) {
        return applyMod(mode(declension), word);
    }

    private static String applyMod(String mod, String word) { // the original method
        if (mod.equals(RuleLibrary.KEEP_MOD)) {
            return word;
        }
        if (mod.indexOf(RuleLibrary.REMOVE_CHARACTER) < 0) {
            return word + mod;
        }
        String result = word;
        for (int i = 0; i < mod.length(); i++) {
            if (mod.charAt(i) == RuleLibrary.REMOVE_CHARACTER) {
                result = result.substring(0, result.length() - 1);
            } else {
                result += mod.substring(i);
                break;
            }
        }
        return result;
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

    public boolean matchAnimated(Boolean animated) {
        return matchBoolean(animated, this.animated);
    }

    public boolean matchGender(Gender gender) {
        // weird logic by historical reasons
        return this.gender == Gender.NEUTER || this.gender == gender;
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
                gender, plural, animated, Arrays.toString(test), Arrays.toString(mods));
    }
}

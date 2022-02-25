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
    public final Gender gender;
    public final boolean plural;
    public final boolean inanimate;

    private final String[] test;
    private final String[] mods;

    public Rule(String[] test, String[] mods, Gender gender, boolean inanimate, boolean plural) {
        this.gender = Objects.requireNonNull(gender);
        this.plural = plural;
        this.inanimate = inanimate;
        this.test = Objects.requireNonNull(test);
        this.mods = Objects.requireNonNull(mods);
    }

    public Stream<String> test() {
        return Arrays.stream(test);
    }

    public String mode(Case declension) {
        return mods[declension.ordinal() - 1];
    }
}

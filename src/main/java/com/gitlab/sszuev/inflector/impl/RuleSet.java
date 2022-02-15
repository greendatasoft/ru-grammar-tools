package com.gitlab.sszuev.inflector.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Created by @ssz on 04.12.2020.
 */
public class RuleSet {
    private final Rule[] exceptions;
    private final Rule[] suffixes;

    public RuleSet(Rule[] exceptions, Rule[] suffixes) {
        this.exceptions = Objects.requireNonNull(exceptions);
        this.suffixes = Objects.requireNonNull(suffixes);
    }

    public Stream<Rule> exceptions() {
        return Arrays.stream(exceptions);
    }

    public Stream<Rule> suffixes() {
        return Arrays.stream(suffixes);
    }
}

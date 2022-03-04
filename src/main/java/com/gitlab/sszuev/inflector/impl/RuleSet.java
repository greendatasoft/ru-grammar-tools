package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.Gender;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

    public static Rule findRule(String word, Gender gender, Boolean animated, Boolean plural, RuleSet rules) {
        Rule exceptionRule = selectRule(rules.exceptions(), word, gender, animated, plural);
        if (exceptionRule != null && exceptionRule.matchGenderStrict(gender)) {
            return exceptionRule;
        }
        Rule suffixRule = selectRule(rules.suffixes(), word, gender, animated, plural);
        if (suffixRule != null && suffixRule.matchGenderStrict(gender)) {
            return suffixRule;
        }
        return exceptionRule != null ? exceptionRule : suffixRule;
    }

    private static Rule selectRule(Stream<Rule> rules, String word, Gender gender, Boolean animated, Boolean plural) {
        List<Rule> res = rules.filter(rule -> filterRule(rule, word, gender, animated, plural))
                .collect(Collectors.toList());
        if (res.isEmpty()) {
            return null;
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        Rule rule = res.stream().filter(x -> x.matchGenderStrict(gender)).findFirst().orElse(null);
        if (rule != null) {
            return rule;
        }
        // if nothing found try neuter
        rule = res.stream().filter(x -> x.matchGenderStrict(Gender.NEUTER)).findFirst().orElse(null);
        if (rule != null) {
            return rule;
        }
        throw new IllegalStateException();
    }

    private static boolean filterRule(Rule rule, String word, Gender gender, Boolean animated, Boolean plural) {
        return rule.matchPlural(plural) && rule.matchAnimated(animated) && rule.matchGender(gender) &&
                rule.test().anyMatch(word::endsWith);
    }

    private Stream<Rule> exceptions() {
        return Arrays.stream(exceptions);
    }

    private Stream<Rule> suffixes() {
        return Arrays.stream(suffixes);
    }
}

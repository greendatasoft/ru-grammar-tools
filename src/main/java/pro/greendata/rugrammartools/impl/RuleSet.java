package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
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

    private static Rule selectRule(Stream<Rule> rules, String word, Gender gender, Boolean animate, Boolean plural) {
        List<Rule> res = rules.filter(rule -> filterRule(rule, word, gender, animate, plural))
                .collect(Collectors.toList());
        if (res.isEmpty()) {
            return null;
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        List<Rule> byGender = filter(res, x -> x.matchGenderStrict(gender));
        if (byGender.isEmpty()) {
            // if nothing found try neuter (original logic)
            byGender = filter(res, x -> x.matchGenderStrict(Gender.NEUTER));
        }
        if (byGender.isEmpty()) {
            throw new IllegalStateException();
        }
        if (byGender.size() == 1) {
            return byGender.get(0);
        }
        if (plural == null && animate == null) {
            return byGender.get(0);
        }
        if (plural == null) {
            List<Rule> byAnimate = filter(byGender, x -> x.matchAnimateStrict(animate));
            if (byAnimate.size() == 1) {
                return byAnimate.get(0);
            }
        }
        if (animate == null) {
            List<Rule> byPlural = filter(byGender, x -> x.matchPluralStrict(plural));
            if (byPlural.size() == 1) {
                return byPlural.get(0);
            }
        }
        List<Rule> byAnimateAndPlural = filter(byGender, x -> x.matchAnimateStrict(animate) && x.matchPluralStrict(plural));
        if (byAnimateAndPlural.size() == 1) {
            return byAnimateAndPlural.get(0);
        }
        List<Rule> byAnimateOrPlural = filter(byGender, x -> x.matchAnimateStrict(animate) || x.matchPluralStrict(plural));
        if (byAnimateOrPlural.size() == 1) {
            return byAnimateOrPlural.get(0);
        }
        // choose the first ...
        return byGender.get(0);
    }

    private static List<Rule> filter(List<Rule> res, Predicate<Rule> test) {
        return res.stream().filter(test).collect(Collectors.toList());
    }

    private static boolean filterRule(Rule rule, String word, Gender gender, Boolean animated, Boolean plural) {
        return filterRule(rule, gender, animated, plural) && rule.test().anyMatch(word::endsWith);
    }

    private static boolean filterRule(Rule rule, Gender gender, Boolean animated, Boolean plural) {
        return rule.matchPlural(plural) && rule.matchAnimate(animated) && rule.matchGender(gender);
    }

    private Stream<Rule> exceptions() {
        return Arrays.stream(exceptions);
    }

    private Stream<Rule> suffixes() {
        return Arrays.stream(suffixes);
    }
}

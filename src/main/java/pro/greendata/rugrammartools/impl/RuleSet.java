package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.PartOfSpeech;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by @ssz on 04.12.2020.
 */
public class RuleSet {
    private static final Comparator<Rule> BY_ANIMATE_COMPARATOR = Comparator.comparingInt((Rule r) -> toInt(r.animate)).reversed();
    private static final Comparator<Rule> BY_PLURAL_COMPARATOR = Comparator.comparingInt((Rule r) -> toInt(r.plural)).reversed();
    private static final Comparator<Rule> BY_PART_OF_SPEECH_COMPARATOR = Comparator.comparingInt((Rule r) -> toInt(r.partOfSpeech)).reversed();

    private final List<Rule> exceptions;
    private final List<Rule> suffixes;

    protected RuleSet(List<Rule> exceptions, List<Rule> suffixes) {
        this.exceptions = Objects.requireNonNull(exceptions);
        this.suffixes = Objects.requireNonNull(suffixes);
    }

    public static Rule findRule(String word,
                                Gender gender,
                                PartOfSpeech partOfSpeech,
                                Boolean animate,
                                Boolean plural,
                                RuleSet rules) {
        Rule exceptionRule = findRule(rules.exceptions, word, gender, partOfSpeech, animate, plural);
        if (exceptionRule != null && exceptionRule.matchGenderStrict(gender)) {
            return exceptionRule;
        }
        Rule suffixRule = findRule(rules.suffixes, word, gender, partOfSpeech, animate, plural);
        if (suffixRule != null && suffixRule.matchGenderStrict(gender)) {
            return suffixRule;
        }
        return exceptionRule != null ? exceptionRule : suffixRule;
    }

    private static Rule findRule(List<Rule> rules, String word, Gender gender, PartOfSpeech pos, Boolean animate, Boolean plural) {
        List<Rule> byEnding = rules.stream().filter(r -> r.match(word)).collect(Collectors.toList());
        if (byEnding.isEmpty()) {
            return null;
        }
        if (byEnding.size() == 1) {
            return byEnding.get(0);
        }
        // if nothing found use neuter as default (it's original weird logic)
        List<Rule> byGender = byEnding.stream()
                .filter(r -> r.matchGenderStrict(gender) || r.matchGenderStrict(Gender.NEUTER))
                .collect(Collectors.toList());
        if (byGender.isEmpty()) { // gender is mandatory right now
            throw new IllegalStateException();
        }
        if (byGender.size() == 1) {
            return byGender.get(0);
        }
        if (plural == null && animate == null && pos == null) {
            // no filter parameters is specified -> return the first
            return byGender.get(0);
        }
        Comparator<Rule> comp = createRuleComparator(pos, animate, plural);
        if (comp == null) {
            // can't sort -> return the first one
            return byGender.get(0);
        }
        return byGender.stream()
                .sorted(comp)
                .filter(r -> r.matchAnimateLenient(animate) && r.matchPluralLenient(plural) && r.matchPartOfSpeechLenient(pos))
                .findFirst().orElse(null);
    }

    private static Comparator<Rule> createRuleComparator(PartOfSpeech pos, Boolean animate, Boolean plural) {
        Comparator<Rule> res = null;
        if (animate != null) {
            res = BY_ANIMATE_COMPARATOR;
        }
        if (plural != null) {
            res = res == null ? BY_PLURAL_COMPARATOR : res.thenComparing(BY_PLURAL_COMPARATOR);
        }
        if (pos != null) {
            res = res == null ? BY_PART_OF_SPEECH_COMPARATOR : res.thenComparing(BY_PART_OF_SPEECH_COMPARATOR);
        }
        return res;
    }

    private static <X> int toInt(X v) {
        return v == null ? -1 : 1;
    }

}

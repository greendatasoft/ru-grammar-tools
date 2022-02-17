package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.Case;
import com.gitlab.sszuev.inflector.Gender;
import com.gitlab.sszuev.inflector.InflectorEngine;
import com.gitlab.sszuev.inflector.WordType;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The engine impl.
 * <p>
 * Created by @ssz on 27.11.2020.
 *
 * @see <a href='https://github.com/petrovich4j/petrovich4j'>petrovich4j</a>
 */
public class InflectorEngineImpl implements InflectorEngine {

    @Override
    public String inflect(String word, WordType type, Gender gender, Case declension) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("No name is given");
        }
        if (declension == Case.NOMINATIVE) {
            return word;
        }
        if (type == WordType.REGULAR_TERM) {
            return inflectRegularTerm(word, declension);
        }
        if (type == WordType.NUMERALS) {
            return inflectNumeral(word, gender, declension);
        }
        return process(word, type, gender == null ? Gender.MALE : gender, declension);
    }

    /**
     * Inflects the numeral.
     *
     * @param number     {@code String}, not {@code null}
     * @param gender     {@link Gender} some numerals have gender (e.g. {@code "oдин"\"одна"\"одно"}),
     *                   but usually it is {@link Gender#NEUTER}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a numeral phrase in the selected case
     */
    public String inflectNumeral(String number, Gender gender, Case declension) {
        return process(number, WordType.NUMERALS, gender, declension);
    }

    /**
     * Inflects the job-title (profession).
     *
     * @param phrase     {@code String}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a phrase in the selected case
     */
    @Override
    public String inflectRegularTerm(String phrase, Case declension) {
        Objects.requireNonNull(declension);
        String[] parts = Objects.requireNonNull(phrase).trim().split("\\s+");

        Gender gender = null; // the gender of word is determined by the phrase; may not match the true gender of the wearer.
        int noun = 0; // the position of the main word in the phrase
        for (int i = 0; i < parts.length; i++) {
            String w = parts[i];
            // preposition
            if (GrammarUtils.canBeNonDerivativePreposition(w)) {
                break;
            }
            // (masculine) skip leading adjectives
            if ((gender == null || gender == Gender.MALE) && GrammarUtils.canBeSingularNominativeMasculineAdjective(w)) {
                gender = Gender.MALE;
                if (GrammarUtils.canBeMasculineAdjectiveBasedSubstantivatNoun(w)) {
                    noun = i;
                    break;
                }
                continue;
            }
            // (feminine) skip leading adjectives
            if ((gender == null || gender == Gender.FEMALE) && GrammarUtils.canBeSingularNominativeFeminineAdjective(w)) {
                gender = Gender.FEMALE;
                if (GrammarUtils.canBeFeminineAdjectiveBasedSubstantivatNoun(w)) {
                    noun = i;
                    break;
                }
                continue;
            }
            noun = i;
            break;
        }
        if (gender == null) {
            // the masculine gender is most common in russian job-titles
            gender = parts.length == 1 && GrammarUtils.canBeFeminineNoun(parts[0]) ? Gender.FEMALE : Gender.MALE;
        }

        // only the first part of the phrase is declined - the main word (noun) and the adjectives surrounding it;
        // the words after - usually does not decline (we assume that some supplemental part goes next)
        int end = noun;
        for (int i = noun + 1; i < parts.length; i++) {
            if (!canBeAdjective(parts[i], gender)) {
                break;
            }
            end = i;
        }
        if (end != noun && end != parts.length - 1) {
            // TODO: phrases with two nouns are ignored for now
            end = noun;
        }
        for (int i = 0; i <= end; i++) {
            parts[i] = processWithHyphen(parts[i], WordType.REGULAR_TERM, gender, declension);
        }
        return String.join(" ", parts);
    }

    private static boolean canBeAdjective(String word, Gender gender) {
        if (gender == Gender.MALE)
            return canBeMasculineAdjective(word);
        if (gender == Gender.FEMALE)
            return canBeFeminineAdjective(word);
        return false;
    }

    private static boolean canBeMasculineAdjective(String word) {
        return GrammarUtils.canBeSingularNominativeMasculineAdjective(word)
                && !GrammarUtils.canBeMasculineAdjectiveBasedSubstantivatNoun(word);
    }

    private static boolean canBeFeminineAdjective(String word) {
        return GrammarUtils.canBeSingularNominativeFeminineAdjective(word)
                && !GrammarUtils.canBeFeminineAdjectiveBasedSubstantivatNoun(word);
    }

    @SuppressWarnings("SameParameterValue")
    private String processWithHyphen(String input, WordType type, Gender gender, Case declension) {
        StringBuilder res = new StringBuilder();
        int prev = 0;
        Matcher m = Pattern.compile("-").matcher(input);
        Gender g = gender;
        while (m.find()) {
            String txt = process(input.substring(prev, m.start()), type, g, declension);
            res.append(txt).append(m.group());
            prev = m.end();
            // the second part is usually in the masculine gender (e.g. "сестра-анестезист")
            g = Gender.MALE;
        }
        res.append(process(input.substring(prev), type, g, declension));
        return res.toString();
    }

    private String process(String phrase, WordType type, Gender gender, Case declension) {
        Rule rule = findRule(phrase, gender, chooseRuleSet(type));
        return rule == null ? phrase : applyMod(rule.mode(declension), phrase);
    }

    private RuleSet chooseRuleSet(WordType type) {
        switch (type) {
            case FIRST_NAME:
                return RuleLibrary.FIRST_NAME_RULES;
            case PATRONYMIC_NAME:
                return RuleLibrary.PATRONYMIC_NAME_RULES;
            case FAMILY_NAME:
                return RuleLibrary.LAST_NAME_RULES;
            case REGULAR_TERM:
                return RuleLibrary.REGULAR_TERM_RULES;
            case NUMERALS:
                return RuleLibrary.NUMERALS_RULES;
            default:
                throw new IllegalArgumentException("Wrong type " + type);
        }
    }

    private static String applyMod(String mod, String name) { // the original method
        if (mod.equals(RuleLibrary.KEEP_MOD)) {
            return name;
        }
        if (mod.indexOf(RuleLibrary.REMOVE_CHARACTER) < 0) {
            return name + mod;
        }
        String result = name;
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

    public static Rule findRule(String phrase, Gender gender, RuleSet rules) {
        Rule exceptionRule = selectRule(rules.exceptions(), gender, phrase);
        if (exceptionRule != null && exceptionRule.gender == gender) {
            return exceptionRule;
        }
        Rule suffixRule = selectRule(rules.suffixes(), gender, phrase);
        if (suffixRule != null && suffixRule.gender == gender) {
            return suffixRule;
        }
        return exceptionRule != null ? exceptionRule : suffixRule;
    }

    private static Rule selectRule(Stream<Rule> rules, Gender gender, String word) {
        String lcWord = word.toLowerCase();
        List<Rule> res = rules.filter(rule -> (rule.gender == Gender.NEUTER || rule.gender == gender)
                        && rule.test().anyMatch(lcWord::endsWith))
                .collect(Collectors.toList());
        if (res.isEmpty()) {
            return null;
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        Rule rule = res.stream().filter(x -> x.gender == gender).findFirst().orElse(null);
        if (rule != null) {
            return rule;
        }
        rule = res.stream().filter(x -> x.gender == Gender.NEUTER).findFirst().orElse(null);
        if (rule != null) {
            return rule;
        }
        throw new IllegalStateException();
    }
}

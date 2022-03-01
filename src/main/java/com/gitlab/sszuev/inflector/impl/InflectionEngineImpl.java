package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.Case;
import com.gitlab.sszuev.inflector.Gender;
import com.gitlab.sszuev.inflector.InflectionEngine;
import com.gitlab.sszuev.inflector.WordType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The engine impl.
 * <p>
 * Created by @ssz on 27.11.2020.
 */
public class InflectionEngineImpl implements InflectionEngine {

    @Override
    public String inflect(String word, WordType type, Case declension, Gender gender, Boolean animated, Boolean plural) {
        require(word, "word");
        require(declension, "declension case");
        require(type, "rule type");
        if (declension == Case.NOMINATIVE) {
            return word;
        }
        return process(word, type, gender == null ? Gender.MALE : gender, declension, animated, plural);
    }

    @Override
    public String inflectNumeral(String number, String unit, Case declension) {
        require(unit, "unit");
        require(declension, "declension");
        Gender gender = GrammarUtils.guessGenderOfSingularNoun(unit);
        String[] parts = checkAndSplit(number);
        int last = parts.length - 1;
        parts[last] = GrammarUtils.changeGenderFormOfNumeral(parts[last], gender);
        String res;
        if (declension == Case.NOMINATIVE) {
            res = String.join(" ", parts);
        } else {
            res = inflectNumeral(parts, declension);
        }
        return res + " " + inflectUnit(unit, number, declension, gender);
    }

    /**
     * Inflects unit.
     *
     * @param unit       {@code String}
     * @param number     {@code String}
     * @param declension {@link Case}
     * @param gender     {@link Gender}
     * @return {@code String}
     * @see <a href='https://numeralonline.ru/10000'>Склонение 10000 по падежам</a>
     */
    protected String inflectUnit(String unit, String number, Case declension, Gender gender) {
        if (GrammarUtils.isZeroNumeral(number)) {
            // NOMINATIVE, GENITIVE,   DATIVE,     ACCUSATIVE, INSTRUMENTAL,PREPOSITIONAL
            // ноль рублей,ноля рублей,нолю рублей,ноль рублей,нолём рублей,ноле рублей
            return inflect(GrammarUtils.toPluralNoun(unit), WordType.GENERIC_NOUN, Case.GENITIVE, gender, null, true);
        }
        if (GrammarUtils.isFractionNumeral(number)) {
            // рубля
            return inflect(unit, WordType.GENERIC_NOUN, Case.GENITIVE, gender, null, null);
        }
        if (GrammarUtils.isNumeralEndWithNumberOne(number)) {
            // NOMINATIVE,GENITIVE,    DATIVE,      ACCUSATIVE,INSTRUMENTAL,PREPOSITIONAL
            // один рубль,одного рубля,одному рублю,один рубль,одним рублём,одном рубле
            return inflect(unit, WordType.GENERIC_NOUN, declension, gender, null, null);
        }
        if (GrammarUtils.isNumeralEndWithTwoThreeFour(number)) {
            // NOMINATIVE,     GENITIVE,          DATIVE,            ACCUSATIVE,     INSTRUMENTAL,        PREPOSITIONAL
            // сорок два рубля,сорока двух рублей,сорока двум рублям,сорок два рубля,сорока двумя рублями,сорока двух рублях
            if (declension == Case.NOMINATIVE || declension == Case.ACCUSATIVE) {
                return inflect(unit, WordType.GENERIC_NOUN, Case.GENITIVE, gender, null, null);
            } else {
                return inflect(GrammarUtils.toPluralNoun(unit), WordType.GENERIC_NOUN, declension, gender, null, true);
            }
        }
        // NOMINATIVE,   GENITIVE,     DATIVE,       ACCUSATIVE,   INSTRUMENTAL,   PREPOSITIONAL
        // десять рублей,десяти рублей,десяти рублям,десять рублей,десятью рублями,десяти рублях
        // NOMINATIVE,         GENITIVE,           DATIVE,               ACCUSATIVE,         INSTRUMENTAL,            PREPOSITIONAL
        // десять тысяч рублей,десяти тысяч рублей,десяти тысячам рублям,десять тысяч рублей,десятью тысячами рублями,десяти тысячах рублях
        if (declension == Case.NOMINATIVE || declension == Case.ACCUSATIVE) {
            declension = Case.GENITIVE;
        }
        return inflect(GrammarUtils.toPluralNoun(unit), WordType.GENERIC_NOUN, declension, gender, null, true);
    }

    @Override
    public String inflectNumeral(String number, Case declension) {
        if (require(declension, "declension case") == Case.NOMINATIVE) {
            return number;
        }
        String[] parts = checkAndSplit(number);
        return inflectNumeral(parts, declension);
    }

    @Override
    public String[] inflectFullName(String[] sfp, Case declension, Gender gender) {
        require(declension, "declension case");
        if (require(sfp, "sfp").length > 3 || sfp.length == 0) {
            throw new IllegalArgumentException();
        }
        if (gender == null) {
            gender = guessGenderByFullName(sfp);
        }
        String s = inflect(sfp[0], WordType.FAMILY_NAME, declension, gender, true, false);
        if (sfp.length == 1) {
            return new String[]{s};
        }
        String f = inflect(sfp[1], WordType.FIRST_NAME, declension, gender, true, false);
        if (sfp.length == 2) {
            return new String[]{s, f};
        }
        String p = inflect(sfp[2], WordType.PATRONYMIC_NAME, declension, gender, true, false);
        return new String[]{s, f, p};
    }

    protected String inflectNumeral(String[] parts, Case declension) {
        String[] res = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String w = parts[i];
            if (i > 0 && "целых".equals(w) && "ноль".equals(parts[i - 1])) {
                res[i] = w; // special case
                continue;
            }
            // each part may have its own gender: "одна тысяча один"
            Gender g = GrammarUtils.guessGenderOfSingleNumeral(w);
            res[i] = inflectNumeral(w, declension, g, null);
        }
        return String.join(" ", res);
    }

    /**
     * Inflects the numeral.
     *
     * @param number     {@code String}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender} some numerals have gender (e.g. {@code "oдин"\"одна"\"одно"}),
     *                   but usually it is {@link Gender#NEUTER}
     * @param plural     {@code boolean}
     * @return {@code String} -  a numeral phrase in the selected case
     */
    public String inflectNumeral(String number, Case declension, Gender gender, Boolean plural) {
        return process(require(number, "numeral"), WordType.NUMERALS, gender, require(declension, "declension"), null, plural);
    }

    /**
     * Inclines a regular-term phrase, which is a combination of words (e.g. job-title, organization name).
     *
     * @param phrase     {@code String}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param animated   - the names of organizations are usually inanimate, the names of professions are animate
     * @return {@code String} - a phrase in the selected case
     */
    @Override
    public String inflectRegularTerm(String phrase, Case declension, Boolean animated) {
        require(declension, "declension case");
        String[] parts = checkAndSplit(phrase);

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
            parts[i] = processWithHyphen(parts[i], WordType.GENERIC_NOUN, gender, declension, animated, false);
        }
        return String.join(" ", parts);
    }

    private static String[] checkAndSplit(String phrase) {
        String[] res = require(phrase, "phrase").trim().split("\\s+");
        if (res.length == 0) {
            throw new IllegalArgumentException();
        }
        return res;
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
    private String processWithHyphen(String input, WordType type, Gender gender, Case declension, Boolean animated, Boolean plural) {
        StringBuilder res = new StringBuilder();
        int prev = 0;
        Matcher m = Pattern.compile("-").matcher(input);
        Gender g = gender;
        while (m.find()) {
            String txt = process(input.substring(prev, m.start()), type, g, declension, animated, plural);
            res.append(txt).append(m.group());
            prev = m.end();
            // the second part is usually in the masculine gender (e.g. "сестра-анестезист")
            g = Gender.MALE;
        }
        res.append(process(input.substring(prev), type, g, declension, animated, plural));
        return res.toString();
    }

    /**
     * Performs the case-inflection operation.
     *
     * @param word       {@code String}
     * @param type       {@link WordType}
     * @param gender     {@link Gender}, can be {@code null}
     * @param declension {@link Case}
     * @param animated   {@link Boolean}, can be {@code null}
     * @param plural     {@link Boolean}, can be {@code null}
     * @return {@code String}
     */
    protected String process(String word, WordType type, Gender gender, Case declension, Boolean animated, Boolean plural) {
        if (type == WordType.GENERIC_NOUN) {
            String res = Dictionary.getNounDictionary().inflect(word, declension, gender, animated, plural);
            if (res != null) {
                return MiscStringUtils.toProperCase(word, res);
            }
        }
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        Rule rule = findRule(nw, gender, animated, plural, chooseRuleSet(type));
        return rule == null ? word : applyMod(rule.mode(declension), word);
    }

    protected Gender guessGenderByFullName(String[] sfp) {
        // by first name
        Gender g;
        if (sfp.length > 1) {
            g = NameUtils.guessGenderByFirstName(sfp[1]);
            if (g != null) {
                return g;
            }
        }
        // by patronymic
        if (sfp.length > 2) {
            g = NameUtils.guessGenderByPatronymicName(sfp[2]);
            if (g != null) {
                return g;
            }
        }
        // family
        return NameUtils.guessGenderBySurname(sfp[0]);
    }

    private RuleSet chooseRuleSet(WordType type) {
        switch (type) {
            case FIRST_NAME:
                return RuleLibrary.FIRST_NAME_RULES;
            case PATRONYMIC_NAME:
                return RuleLibrary.PATRONYMIC_NAME_RULES;
            case FAMILY_NAME:
                return RuleLibrary.LAST_NAME_RULES;
            case GENERIC_NOUN:
                return RuleLibrary.REGULAR_TERM_RULES;
            case NUMERALS:
                return RuleLibrary.NUMERALS_RULES;
            default:
                throw new IllegalArgumentException("Wrong type " + type);
        }
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

    private static Rule findRule(String phrase, Gender gender, Boolean animated, Boolean plural, RuleSet rules) {
        Rule exceptionRule = selectRule(rules.exceptions(), phrase, gender, animated, plural);
        if (exceptionRule != null && exceptionRule.gender == gender) {
            return exceptionRule;
        }
        Rule suffixRule = selectRule(rules.suffixes(), phrase, gender, animated, plural);
        if (suffixRule != null && suffixRule.gender == gender) {
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
        Rule rule = res.stream().filter(x -> x.gender == gender).findFirst().orElse(null);
        if (rule != null) {
            return rule;
        }
        // if nothing found try neuter
        rule = res.stream().filter(x -> x.gender == Gender.NEUTER).findFirst().orElse(null);
        if (rule != null) {
            return rule;
        }
        throw new IllegalStateException();
    }

    private static boolean filterRule(Rule rule, String word, Gender gender, Boolean animated, Boolean plural) {
        return (plural == null || rule.plural == plural) &&
                (animated == null || rule.inanimate != animated) &&
                (rule.gender == Gender.NEUTER || rule.gender == gender) &&
                rule.test().anyMatch(word::endsWith);
    }

    protected static String require(String string, String name) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("No " + name + " is given");
        }
        return string;
    }

    protected static <X> X require(X object, String name) {
        if (object == null) {
            throw new IllegalArgumentException("No " + name + " is given");
        }
        return object;
    }
}

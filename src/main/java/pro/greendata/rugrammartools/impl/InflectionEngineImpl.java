package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Case;
import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.InflectionEngine;
import pro.greendata.rugrammartools.WordType;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The engine impl.
 * <p>
 * Created by @ssz on 27.11.2020.
 */
public class InflectionEngineImpl implements InflectionEngine {

    @Override
    public String inflect(String word, WordType type, Case declension, Gender gender, Boolean animate, Boolean plural) {
        require(word, "word");
        require(declension, "declension case");
        require(type, "rule type");
        if (declension == Case.NOMINATIVE) {
            return word;
        }
        return process(word, type, gender == null ? Gender.MALE : gender, declension, animate, plural);
    }

    @Override
    public String inflectNumeral(String numeral, Case declension) {
        if (require(declension, "declension case") == Case.NOMINATIVE) {
            return numeral;
        }
        String[] parts = checkAndSplit(numeral);
        if (GrammarUtils.canBeOrdinalNumeral(numeral)) {
            return inflectOrdinalNumeral(parts, declension, false);
        }
        return inflectCardinalNumeral(parts, declension);
    }

    @Override
    public String inflectNumeral(String numeral, String unit, Case declension) {
        require(unit, "unit");
        require(declension, "declension");
        String[] parts = checkAndSplit(numeral);
        Optional<Dictionary.Word> info = Dictionary.getNounDictionary().wordInfo(unit);
        Gender gender = info.map(Dictionary.Word::gender).orElseGet(() -> GrammarUtils.guessGenderOfSingularNoun(unit));
        if (GrammarUtils.canBeOrdinalNumeral(numeral)) {
            String res = inflectOrdinalNumeral(parts, declension, gender, info.map(Dictionary.Word::animate).orElse(false));
            return res + " " + inflect(unit, WordType.GENERIC, declension, gender, null, null);
        }
        int last = parts.length - 1;
        parts[last] = GrammarUtils.changeGenderFormOfNumeral(parts[last], gender);
        String res;
        if (declension == Case.NOMINATIVE) {
            res = String.join(" ", parts);
        } else {
            res = inflectCardinalNumeral(parts, declension);
        }
        return res + " " + inflectUnit(unit, numeral, declension, gender);
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
            return inflect(GrammarUtils.toPluralNoun(unit), WordType.GENERIC, Case.GENITIVE, gender, null, true);
        }
        if (GrammarUtils.isFractionNumeral(number)) {
            // рубля
            return inflect(unit, WordType.GENERIC, Case.GENITIVE, gender, null, null);
        }
        if (GrammarUtils.isNumeralEndWithNumberOne(number)) {
            // NOMINATIVE,GENITIVE,    DATIVE,      ACCUSATIVE,INSTRUMENTAL,PREPOSITIONAL
            // один рубль,одного рубля,одному рублю,один рубль,одним рублём,одном рубле
            return inflect(unit, WordType.GENERIC, declension, gender, null, null);
        }
        if (GrammarUtils.isNumeralEndWithTwoThreeFour(number)) {
            // NOMINATIVE,     GENITIVE,          DATIVE,            ACCUSATIVE,     INSTRUMENTAL,        PREPOSITIONAL
            // сорок два рубля,сорока двух рублей,сорока двум рублям,сорок два рубля,сорока двумя рублями,сорока двух рублях
            if (declension == Case.NOMINATIVE || declension == Case.ACCUSATIVE) {
                return inflect(unit, WordType.GENERIC, Case.GENITIVE, gender, null, null);
            } else {
                return inflect(GrammarUtils.toPluralNoun(unit), WordType.GENERIC, declension, gender, null, true);
            }
        }
        // NOMINATIVE,   GENITIVE,     DATIVE,       ACCUSATIVE,   INSTRUMENTAL,   PREPOSITIONAL
        // десять рублей,десяти рублей,десяти рублям,десять рублей,десятью рублями,десяти рублях
        // NOMINATIVE,         GENITIVE,           DATIVE,               ACCUSATIVE,         INSTRUMENTAL,            PREPOSITIONAL
        // десять тысяч рублей,десяти тысяч рублей,десяти тысячам рублям,десять тысяч рублей,десятью тысячами рублями,десяти тысячах рублях
        if (declension == Case.NOMINATIVE || declension == Case.ACCUSATIVE) {
            declension = Case.GENITIVE;
        }
        return inflect(GrammarUtils.toPluralNoun(unit), WordType.GENERIC, declension, gender, null, true);
    }

    protected String inflectCardinalNumeral(String[] parts, Case declension) {
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
        return process(require(number, "numeral"), WordType.NUMERAL, gender, require(declension, "declension"), null, plural);
    }

    protected String inflectOrdinalNumeral(String[] parts, Case declension, Boolean animate) {
        Gender gender = Objects.requireNonNull(GrammarUtils.guessGenderOfSingleAdjective(parts[parts.length - 1]));
        return inflectOrdinalNumeral(parts, declension, gender, animate);
    }

    protected String inflectOrdinalNumeral(String[] parts, Case declension, Gender gender, Boolean animate) {
        String w = parts[parts.length - 1];
        w = inflect(w, WordType.GENERIC, declension, gender, animate, false);
        parts[parts.length - 1] = w;
        return String.join(" ", parts);
    }

    @Override
    public String[] inflectSPF(String[] sfp, Case declension, Gender gender) {
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

    @Override
    public String inflectAny(String phrase, Case declension) {
        require(declension, "null case declension");
        String[] parts = checkAndSplit(phrase);
        if (parts.length < 4) { // then can be full name
            if (parts.length > 1 && NameUtils.isFirstname(parts[1])) {
                return inflectFullName(phrase, declension);
            }
            if (parts.length == 1 && NameUtils.canBeSurname(parts[0])) {
                return inflectFullName(phrase, declension);
            }
            if (parts.length == 3 && NameUtils.canBePatronymic(parts[2]) && NameUtils.canBeSurname(parts[0])) {
                return inflectFullName(phrase, declension);
            }
        }
        return inflectRegularTerm(phrase, declension, null);
    }

    /**
     * Inclines a regular-term phrase, which is a combination of words (e.g. job-title, organization name).
     *
     * @param phrase     {@code String}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param animate    - the names of organizations are usually inanimate, the names of professions are animate
     * @return {@code String} - a phrase in the selected case
     */
    @Override
    public String inflectRegularTerm(String phrase, Case declension, Boolean animate) {
        if (require(declension, "declension case") == Case.NOMINATIVE) {
            return phrase;
        }
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
            // (neuter) skip leading adjectives
            if ((gender == null || gender == Gender.NEUTER) && GrammarUtils.canBeSingularNominativeNeuterAdjective(w)) {
                gender = Gender.NEUTER;
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
            if (!GrammarUtils.canBeAdjective(parts[i], gender)) {
                break;
            }
            end = i;
        }
        if (end != noun && end != parts.length - 1) {
            // TODO: phrases with two nouns are ignored for now
            end = noun;
        }
        for (int i = 0; i <= end; i++) {
            parts[i] = processWithHyphen(parts[i], WordType.GENERIC, gender, declension, animate, false);
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
        if (type == WordType.GENERIC) {
            String res = Dictionary.getNounDictionary().inflect(word, declension, gender, animated, plural);
            if (res != null) {
                return MiscStringUtils.toProperCase(word, res);
            }
        }
        String nw = MiscStringUtils.normalize(word, Dictionary.LOCALE);
        Rule rule = RuleSet.findRule(nw, gender, animated, plural, chooseRuleSet(type));
        return rule == null ? word : rule.apply(declension, word);
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
            case GENERIC:
                return RuleLibrary.REGULAR_TERM_RULES;
            case NUMERAL:
                return RuleLibrary.NUMERALS_RULES;
            default:
                throw new IllegalArgumentException("Wrong type " + type);
        }
    }

    protected static String require(String string, String name) {
        if (string == null || string.isBlank()) {
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

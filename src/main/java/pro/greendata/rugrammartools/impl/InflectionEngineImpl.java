package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Case;
import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.InflectionEngine;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.utils.GrammarUtils;
import pro.greendata.rugrammartools.impl.utils.NameUtils;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The engine impl.
 * <p>
 * Created by @ssz on 27.11.2020.
 */
public class InflectionEngineImpl implements InflectionEngine {

    /**
     * Declines the given {@code word} in accordance with the specified settings using petrovich rules.
     * This is the generic method.
     *
     * @param word       {@code String}, single word or phrase, a term, in nominative case, not {@code null}
     * @param type       {@link RuleType}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender} feminine, masculine or neuter,
     *                   {@code null} to choose automatically (usually it is {@link Gender#MALE}) or for undefined cases
     * @param animate    {@code Boolean} can be specified if {@code type = } {@link RuleType#GENERIC},
     *                   {@code null} for default behaviour
     * @param plural     {@code Boolean} if {@code true} then plural,
     *                   {@code false} for singular or {@code null} for default behaviour,
     *                   makes sense only if {@code type = } {@link RuleType#GENERIC}
     * @return {@code String}
     */
    public String inflect(String word, RuleType type, Case declension, Gender gender, Boolean animate, Boolean plural) {
        require(word, "word");
        require(declension, "declension case");
        require(type, "rule type");
        if (declension == Case.NOMINATIVE) {
            return word;
        }
        String res = processRule(TextUtils.normalize(word, Dictionary.LOCALE),
                type, declension, gender == null ? Gender.MALE : gender, animate, plural);
        return res == null ? word : TextUtils.toProperCase(word, res);
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
        return inflectCardinalNumeral(parts, declension, null);
    }

    @Override
    public String inflectNumeral(String numeral, String unit, Case declension) {
        require(unit, "unit");
        require(declension, "declension");
        String[] parts = checkAndSplit(numeral);
        Phrase phrase = Phrase.parse(unit, null, null);
        int last = parts.length - 1;
        String res;
        if (GrammarUtils.canBeOrdinalNumeral(numeral)) {
            parts[last] = GrammarUtils.changeGenderOfOrdinalNumeral(parts[last], phrase.gender());
            if (declension == Case.NOMINATIVE) {
                res = String.join(" ", parts);
            } else {
                res = inflectOrdinalNumeral(parts, declension, phrase.gender(), phrase.animate());
            }
            return res + " " + inflectPhrase(phrase, declension, null);
        }
        parts[last] = GrammarUtils.changeGenderOfCardinalNumeral(parts[last], phrase.gender());
        if (declension == Case.NOMINATIVE) {
            res = String.join(" ", parts);
        } else {
            // rule: Дробные числительные не сочетаются с одушевленными именами существительными: нельзя делить живое на части.
            res = inflectCardinalNumeral(parts, declension, GrammarUtils.isFractionNumeral(numeral) ? Boolean.FALSE : phrase.animate());
        }
        return res + " " + inflectUnit(phrase, numeral, declension);
    }

    /**
     * Inflects unit.
     *
     * @param unit       {@link Phrase}
     * @param number     {@code String}
     * @param declension {@link Case}
     * @return {@code String}
     * @see <a href='https://numeralonline.ru/10000'>Склонение 10000 по падежам</a>
     */
    protected String inflectUnit(Phrase unit, String number, Case declension) {
        if (GrammarUtils.isZeroNumeral(number)) {
            // NOMINATIVE, GENITIVE,   DATIVE,     ACCUSATIVE, INSTRUMENTAL,PREPOSITIONAL
            // ноль рублей,ноля рублей,нолю рублей,ноль рублей,нолём рублей,ноле рублей
            return inflectPhrase(unit, Case.GENITIVE, true);
        }
        if (GrammarUtils.isFractionNumeral(number)) {
            // рубля (consider as inanimate)
            return inflectPhrase(unit, Case.GENITIVE, false);
        }
        if (GrammarUtils.isNumeralEndWithNumberOne(number)) {
            // NOMINATIVE,GENITIVE,    DATIVE,      ACCUSATIVE,INSTRUMENTAL,PREPOSITIONAL
            // один рубль,одного рубля,одному рублю,один рубль,одним рублём,одном рубле
            return inflectPhrase(unit, declension, false);
        }
        if (GrammarUtils.isNumeralEndWithTwoThreeFour(number)) {
            // NOMINATIVE,     GENITIVE,          DATIVE,            ACCUSATIVE,     INSTRUMENTAL,        PREPOSITIONAL
            // сорок два рубля,сорока двух рублей,сорока двум рублям,сорок два рубля,сорока двумя рублями,сорока двух рублях
            if (declension == Case.NOMINATIVE || ((unit.animate() == null || !unit.animate()) && declension == Case.ACCUSATIVE)) {
                return inflectPhrase(unit, Case.GENITIVE, false);
            } else {
                return inflectPhrase(unit, declension, true);
            }
        }
        // NOMINATIVE,   GENITIVE,     DATIVE,       ACCUSATIVE,   INSTRUMENTAL,   PREPOSITIONAL
        // десять рублей,десяти рублей,десяти рублям,десять рублей,десятью рублями,десяти рублях
        // NOMINATIVE,         GENITIVE,           DATIVE,               ACCUSATIVE,         INSTRUMENTAL,            PREPOSITIONAL
        // десять тысяч рублей,десяти тысяч рублей,десяти тысячам рублям,десять тысяч рублей,десятью тысячами рублями,десяти тысячах рублях
        if (declension == Case.NOMINATIVE || declension == Case.ACCUSATIVE) {
            declension = Case.GENITIVE;
        }
        return inflectPhrase(unit, declension, true);
    }

    protected String inflectCardinalNumeral(String[] parts, Case declension, Boolean animated) {
        String[] res = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String w = parts[i];
            if (i > 0 && "целых".equals(w) && isBigOrZero(parts[i - 1])) {
                res[i] = w; // special case
                continue;
            }
            // each part may have its own gender: "одна тысяча один"
            Gender g = GrammarUtils.guessGenderOfSingleNumeral(w);
            res[i] = inflectCardinalNumeral(w, declension, g, animated);
        }
        return String.join(" ", res);
    }

    private static boolean isBigOrZero(String w) {
        if ("ноль".equals(w) || "тысячи".equals(w) || "тысяч".equals(w)) {
            // ноль целых две десятых, десять тысяч целых тридцать три сотых
            return true;
        }
        for (String s : SpellingEngineImpl.BIGS) {
            // два миллиона целых две десятых, миллион целых одна сотая
            if (w.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inflects the numeral.
     *
     * @param number     {@code String}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender} some numerals have gender (e.g. {@code "oдин"\"одна"\"одно"}),
     *                   but usually it is {@link Gender#NEUTER}
     * @param animated   {@link Boolean}
     * @return {@code String} -  a numeral phrase in the selected case
     */
    protected String inflectCardinalNumeral(String number, Case declension, Gender gender, Boolean animated) {
        return inflect(number, RuleType.NUMERAL, require(declension, "declension"), gender, animated, null);
    }

    protected String inflectOrdinalNumeral(String[] parts, Case declension, Boolean animate) {
        Gender gender = Objects.requireNonNull(GrammarUtils.guessGenderOfSingleAdjective(parts[parts.length - 1]));
        return inflectOrdinalNumeral(parts, declension, gender, animate);
    }

    protected String inflectOrdinalNumeral(String[] parts, Case declension, Gender gender, Boolean animate) {
        String w = parts[parts.length - 1];
        //w = inflect(w, RuleType.GENERIC, declension, gender, animate, false);
        w = inflectPhrase(w, declension, gender, animate, false);
        parts[parts.length - 1] = w;
        return String.join(" ", parts);
    }

    @Override
    public String inflectFirstname(String firstname, Case declension, Gender gender) {
        gender = Optional.ofNullable(gender).orElseGet(() -> NameUtils.guessGenderByFirstName(firstname));
        return inflect(firstname, RuleType.FIRST_NAME, declension, gender, true, false);
    }

    @Override
    public String inflectPatronymic(String middlename, Case declension, Gender gender) {
        gender = Optional.ofNullable(gender).orElseGet(() -> NameUtils.guessGenderByPatronymicName(middlename));
        return inflect(middlename, RuleType.PATRONYMIC_NAME, declension, gender, true, false);
    }

    @Override
    public String inflectSurname(String surname, Case declension, Gender gender) {
        gender = Optional.ofNullable(gender).orElseGet(() -> NameUtils.guessGenderBySurname(surname));
        return inflect(surname, RuleType.FAMILY_NAME, declension, gender, true, false);
    }

    @Override
    public String inflectNameOfProfession(String profession, Case declension) {
        return inflectPhrase(profession, declension, null, true, false);
    }

    @Override
    public String inflectNameOfOrganization(String organization, Case declension) {
        return inflectPhrase(organization, declension, null, false, false);
    }

    @Override
    public String inflectFullname(String sfp, Case declension) {
        return String.join(" ", inflectSPF(require(sfp, "surname+firstname+patronymic").split("\\s+"),
                require(declension, "declension"), null));
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
        String s = inflect(sfp[0], RuleType.FAMILY_NAME, declension, gender, true, false);
        if (sfp.length == 1) {
            return new String[]{s};
        }
        String f = inflect(sfp[1], RuleType.FIRST_NAME, declension, gender, true, false);
        if (sfp.length == 2) {
            return new String[]{s, f};
        }
        String p = inflect(sfp[2], RuleType.PATRONYMIC_NAME, declension, gender, true, false);
        return new String[]{s, f, p};
    }

    @Override
    public String inflectAny(String phrase, Case declension) {
        require(declension, "null case declension");
        String[] parts = checkAndSplit(phrase);
        if (parts.length < 4) { // then can be full name
            if (parts.length > 1 && NameUtils.isFirstname(parts[1])) {
                return inflectFullname(phrase, declension);
            }
            if (parts.length == 1 && NameUtils.canBeSurname(parts[0])) {
                return inflectFullname(phrase, declension);
            }
            if (parts.length == 3 && NameUtils.canBePatronymic(parts[2]) && NameUtils.canBeSurname(parts[0])) {
                return inflectFullname(phrase, declension);
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
        return inflectPhrase(phrase, declension, null, animate, null);
    }

    /**
     * Inclines a regular-term phrase, which is a combination of words (e.g. job-title, organization name).
     *
     * @param phrase     {@code String}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender}, can be {@code null}
     * @param animate    {@code Boolean} can be {@code null}
     * @param plural     {@code Boolean}, can be {@code null}
     * @return {@code String} - a phrase in the selected case
     */
    public String inflectPhrase(String phrase, Case declension, Gender gender, Boolean animate, Boolean plural) {
        if (require(declension, "declension case") == Case.NOMINATIVE) {
            return phrase;
        }
        return inflectPhrase(Phrase.parse(phrase, gender, animate), declension, plural);
    }

    /**
     * Inclines a regular-term phrase, which is a combination of words, separated by space.
     *
     * @param phrase     {@link Phrase}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param plural     {@code Boolean}
     * @return {@code String} - a phrase in the selected case
     */
    public String inflectPhrase(Phrase phrase, Case declension, Boolean plural) {
        if (require(declension, "declension case") == Case.NOMINATIVE) {
            return phrase.raw();
        }
        if (plural == null) {
            plural = phrase.plural();
        }
        List<String> res = new ArrayList<>();
        for (int i = 0; i < phrase.length(); i++) {
            Word detail = phrase.details(i);
            String orig = phrase.original(i);
            if (detail.isIndeclinable()) {
                res.add(orig);
                continue;
            }
            String k = phrase.key(i);
            String w = processRegularWord(k, detail, declension, plural);
            res.add(w == null ? orig : TextUtils.toProperCase(orig, w));
        }
        return Phrase.compose(res, phrase.separators());
    }

    private static String[] checkAndSplit(String phrase) {
        String[] res = require(phrase, "phrase").trim().split("\\s+");
        if (res.length == 0) {
            throw new IllegalArgumentException();
        }
        return res;
    }

    /**
     * Inflects a regular word (noun, adjective, etc).
     *
     * @param key        {@code String} a normalized word
     * @param details    {@link Word}, not {@code null}
     * @param declension {@link Case}, not {@code null}
     * @param toPlural   {@code Boolean}
     * @return {@code String} or {@code null}
     */
    protected String processRegularWord(String key, Word details, Case declension, Boolean toPlural) {
        String res = processDictionaryRecord(details, declension, toPlural);
        // indeclinable words are skipped upper on the stack, if null - then the word is incomplete, try petrovich
        if (res != null) {
            return res;
        }
        if (toPlural != null && toPlural) {
            key = GrammarUtils.toPluralNoun(key);
        }
        return processRule(key, RuleType.GENERIC, declension, details.gender(), details.animate(), toPlural);
    }

    /**
     * Inflects a word using petrovich rules.
     *
     * @param record     {@link Word}, not {@code null}
     * @param declension {@link Case}, not {@code null}
     * @param plural     {@code Boolean}, filter parameter, can be {@code null}
     * @return {@code String} or {@code null}
     */
    protected String processDictionaryRecord(Word record, Case declension, Boolean plural) {
        if (declension == Case.NOMINATIVE) {
            return plural == Boolean.TRUE ? record.plural() : null;
        }
        String[] cases = plural == Boolean.TRUE && record.pluralCases() != null ? record.pluralCases() : record.singularCases();
        if (cases == null) {
            return null;
        }
        String w = cases[declension.ordinal() - 1];
        return selectLongestWord(w);
    }

    /**
     * Inflects a word using petrovich rules.
     *
     * @param normalized {@code String}, not {@code null}
     * @param type       {@link RuleType}, not {@code null}
     * @param declension {@link Case}, not {@code null}
     * @param gender     {@link Gender}, filter parameter
     * @param animate    {@code Boolean}, filter parameter, can be {@code null}
     * @param plural     {@code Boolean}, filter parameter, can be {@code null}
     * @return {@code String} or {@code null}
     */
    protected String processRule(String normalized, RuleType type, Case declension, Gender gender, Boolean animate, Boolean plural) {
        Rule rule = RuleSet.findRule(normalized, gender, animate, plural, chooseRuleSet(type));
        if (rule == null) {
            return null;
        }
        return rule.apply(declension, normalized);
    }

    /**
     * Selects the longest word, if it has separator ',' (sometimes there is two or more correct forms).
     *
     * @param w {@code String}
     * @return {@code String}
     */
    private static String selectLongestWord(String w) {
        if (!w.contains(",")) {
            return w;
        }
        String[] array = w.split(",\\s*");
        String res = null;
        for (String s : array) {
            if (res == null) {
                res = s;
            } else if (s.length() > res.length()) {
                res = s;
            }
        }
        return res;
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

    private RuleSet chooseRuleSet(RuleType type) {
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

    private static String require(String string, String name) {
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

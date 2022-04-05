package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Case;
import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.InflectionEngine;
import pro.greendata.rugrammartools.impl.Phrase.Type;
import pro.greendata.rugrammartools.impl.dictionaries.AdjectiveDictionary;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.dictionaries.NounDictionary;
import pro.greendata.rugrammartools.impl.utils.*;

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
     * @param word       {@code String}, single noun or phrase with noun, a term, in nominative case, not {@code null}
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
        String res = processRule(TextUtils.normalize(word),
                type, declension, gender == null ? Gender.MALE : gender, PartOfSpeech.NOUN, animate, plural);
        return res == null ? word : TextUtils.toProperCase(word, res);
    }

    @Override
    public String inflectNumeral(String numeral, Case declension) {
        if (require(declension, "declension case") == Case.NOMINATIVE) {
            return numeral;
        }
        String[] parts = checkAndSplit(numeral);
        if (NumeralUtils.canBeOrdinalNumeral(numeral)) {
            return inflectOrdinalNumeral(parts, declension, false);
        }
        return inflectCardinalNumeral(parts, declension, null);
    }

    @Override
    public String inflectNumeral(String numeral, String unit, Case declension) {
        require(unit, "unit");
        require(declension, "declension");
        String[] parts = checkAndSplit(numeral);
        Phrase phrase = Phrase.parse(unit, Type.ANY, null, null);
        int last = parts.length - 1;
        String res;
        if (NumeralUtils.canBeOrdinalNumeral(numeral)) {
            parts[last] = NumeralUtils.changeGenderOfOrdinalNumeral(parts[last], phrase.gender());
            if (declension == Case.NOMINATIVE) {
                res = String.join(" ", parts);
            } else {
                res = inflectOrdinalNumeral(parts, declension, phrase.gender(), phrase.animate());
            }
            return res + " " + inflectPhrase(phrase, declension, null);
        }
        parts[last] = NumeralUtils.changeGenderOfCardinalNumeral(parts[last], phrase.gender());
        if (declension == Case.NOMINATIVE) {
            res = String.join(" ", parts);
        } else {
            // rule: Дробные числительные не сочетаются с одушевленными именами существительными: нельзя делить живое на части.
            res = inflectCardinalNumeral(parts, declension, NumeralUtils.canBeFraction(numeral) ? Boolean.FALSE : phrase.animate());
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
        if (NumeralUtils.isZero(number)) {
            // NOMINATIVE, GENITIVE,   DATIVE,     ACCUSATIVE, INSTRUMENTAL,PREPOSITIONAL
            // ноль рублей,ноля рублей,нолю рублей,ноль рублей,нолём рублей,ноле рублей
            return inflectPhrase(unit, Case.GENITIVE, true);
        }
        if (NumeralUtils.canBeFraction(number)) {
            // рубля (consider as inanimate)
            return inflectPhrase(unit, Case.GENITIVE, false);
        }
        if (NumeralUtils.endsWithCardinalOne(number)) {
            // NOMINATIVE,GENITIVE,    DATIVE,      ACCUSATIVE,INSTRUMENTAL,PREPOSITIONAL
            // один рубль,одного рубля,одному рублю,один рубль,одним рублём,одном рубле
            return inflectPhrase(unit, declension, false);
        }
        if (NumeralUtils.endsWithCardinalTwoThreeFour(number)) {
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
            if (i > 0 && "целых".equalsIgnoreCase(w) &&
                    (NumeralUtils.isZero(parts[i - 1]) || NumeralUtils.containsBigCardinalNumeral(parts[i - 1]))) {
                res[i] = w; // special case
                continue;
            }
            // each part may have its own gender: "одна тысяча один"
            Gender g = NumeralUtils.guessGenderOfSingleNumeral(w);
            res[i] = inflectCardinalNumeral(w, declension, g, animated);
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
        w = inflectPhrase(w, Type.ANY, declension, gender, animate, false);
        parts[parts.length - 1] = w;
        return String.join(" ", parts);
    }

    @Override
    public String inflectFirstname(String firstname, Case declension, Gender gender) {
        gender = Optional.ofNullable(gender).orElseGet(() -> HumanNameUtils.guessGenderByFirstName(firstname));
        return inflect(firstname, RuleType.FIRST_NAME, declension, gender, true, false);
    }

    @Override
    public String inflectPatronymic(String middlename, Case declension, Gender gender) {
        gender = Optional.ofNullable(gender).orElseGet(() -> HumanNameUtils.guessGenderByPatronymicName(middlename));
        return inflect(middlename, RuleType.PATRONYMIC_NAME, declension, gender, true, false);
    }

    @Override
    public String inflectSurname(String surname, Case declension, Gender gender) {
        gender = Optional.ofNullable(gender).orElseGet(() -> HumanNameUtils.guessGenderBySurname(surname));
        return inflect(surname, RuleType.FAMILY_NAME, declension, gender, true, false);
    }

    @Override
    public String inflectNameOfProfession(String profession, Case declension) {
        return inflectPhrase(profession, Type.PROFESSION_NAME, declension, null, true, false);
    }

    @Override
    public String inflectNameOfOrganization(String organization, Case declension) {
        return inflectPhrase(organization, Type.ORGANIZATION_NAME, declension, null, false, false);
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
            gender = HumanNameUtils.guessGenderByFullName(sfp);
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
            if (parts.length > 1 && HumanNameUtils.isFirstname(parts[1])) {
                return inflectFullname(phrase, declension);
            }
            if (parts.length == 1 && HumanNameUtils.canBeSurname(parts[0])) {
                return inflectFullname(phrase, declension);
            }
            if (parts.length == 3 && HumanNameUtils.canBePatronymic(parts[2]) && HumanNameUtils.canBeSurname(parts[0])) {
                return inflectFullname(phrase, declension);
            }
        }
        // todo: handle numerals (both cardinal and ordinal)
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
        return inflectPhrase(phrase, Type.ANY, declension, null, animate, null);
    }

    /**
     * Inclines a regular-term phrase, which is a combination of words (e.g. job-title, organization name).
     *
     * @param phrase     {@code String}, not {@code null}
     * @param type       {@link Type}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender}, can be {@code null}
     * @param animate    {@code Boolean} can be {@code null}
     * @param plural     {@code Boolean}, can be {@code null}
     * @return {@code String} - a phrase in the selected case
     */
    public String inflectPhrase(String phrase, Type type, Case declension, Gender gender, Boolean animate, Boolean plural) {
        if (require(declension, "declension case") == Case.NOMINATIVE) {
            return phrase;
        }
        return inflectPhrase(Phrase.parse(phrase, type, gender, animate), declension, plural);
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
        Phrase.Mutable pm = phrase.toMutable();
        for (int i = 0; i < pm.length(); i++) {
            Word detail = pm.details(i);
            if (detail.isIndeclinable()) {
                continue;
            }
            String k = pm.key(i);
            String w = processRegularWord(k, detail, declension, plural);
            if (w != null) {
                pm.set(i, w);
            }
        }
        return pm.compose();
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
        RuleType type = details.rule();
        Dictionary.Record record = details.record();
        String res;
        if (type == RuleType.GENERIC && record instanceof NounDictionary.Word) {
            res = processDictionaryNounRecord(key, (NounDictionary.Word) record, declension, toPlural);
        } else if (type == RuleType.GENERIC && record instanceof AdjectiveDictionary.Word) {
            res = processDictionaryAdjectiveRecord(key, (AdjectiveDictionary.Word) record, declension,
                    details, toPlural);
        } else {
            res = null;
        }
        // indeclinable words are skipped upper on the stack, if null - then the word is incomplete, try petrovich
        if (res != null) {
            return res;
        }
        if (toPlural != null && toPlural) {
            // note that for plural declensions the base is also plural in json, not singular like in dictionary rules
            key = GrammarUtils.toPluralNoun(key);
        }
        return processRule(key, type, declension, details.gender(), details.partOfSpeech(), details.animate(), toPlural);
    }

    protected String processDictionaryAdjectiveRecord(String key, AdjectiveDictionary.Word record, Case declension,
                                                      Word detail, Boolean plural) {
        String[] cases;
        if (plural) {
            cases = record.pluralCases();
        } else if (detail.gender() == Gender.MALE) {
            cases = record.masculineCases();
        } else if (detail.gender() == Gender.FEMALE) {
            cases = record.feminineCases();
        } else if (detail.gender() == Gender.NEUTER) {
            cases = record.neuterCases();
        } else {
            return null;
        }

        String w;
        if (declension == Case.ACCUSATIVE && !detail.animate()) {
            w = cases[0];
        } else {
            w = cases[declension.ordinal()];
        }

        return applyCase(key, w);
    }

    /**
     * Inflects the specified word using dictionary.
     *
     * @param key        {@code String} a normalized word
     * @param record     {@link NounDictionary.Word}, not {@code null}
     * @param declension {@link Case}, not {@code null}
     * @param plural     {@code Boolean}, filter parameter, can be {@code null}
     * @return {@code String} or {@code null}
     */
    protected String processDictionaryNounRecord(String key, NounDictionary.Word record, Case declension, Boolean plural) {
        if (declension == Case.NOMINATIVE) {
            return plural == Boolean.TRUE ? record.plural() : null;
        }
        String[] cases = plural == Boolean.TRUE && record.pluralCases() != null ? record.pluralCases() : record.singularCases();
        if (cases == null) {
            return null;
        }
        String w = cases[declension.ordinal() - 1];
        return applyCase(key, w);
    }

    private static String applyCase(String key, String word) {
        if (!word.contains(",")) {
            return RuleUtils.changeEnding(key, word);
        }
        // selects the longest
        String[] array = word.split(",\\s*");
        String res = null;
        for (String s : array) {
            String item = RuleUtils.changeEnding(key, s);
            if (res == null) {
                res = item;
                continue;
            }
            if (item.length() > res.length()) {
                res = item;
            }
        }
        return res;
    }

    /**
     * Inflects a word using petrovich rules.
     *
     * @param normalized {@code String}, not {@code null}
     * @param type       {@link RuleType}, not {@code null}
     * @param declension {@link Case}, not {@code null}
     * @param gender     {@link Gender}, filter parameter
     * @param pos        {@link PartOfSpeech}, filter parameter
     * @param animate    {@code Boolean}, filter parameter, can be {@code null}
     * @param plural     {@code Boolean}, filter parameter, can be {@code null}
     * @return {@code String} or {@code null}
     */
    protected String processRule(String normalized,
                                 RuleType type,
                                 Case declension,
                                 Gender gender,
                                 PartOfSpeech pos,
                                 Boolean animate,
                                 Boolean plural) {
        Rule rule = RuleSet.findRule(normalized, gender, pos, animate, plural, chooseRuleSet(type));
        if (rule == null) {
            return null;
        }
        return rule.apply(declension, normalized);
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

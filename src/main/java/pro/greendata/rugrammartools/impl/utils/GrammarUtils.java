package pro.greendata.rugrammartools.impl.utils;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.PlainDictionary;

import java.util.*;

/**
 * Utilities for working with the Russian language.
 * It contains generic rules, some of them were collected empirically.
 * <p>
 *
 * @see NumeralUtils
 * @see HumanNameUtils
 */
public class GrammarUtils {

    // collection of words that are definitely not adjectives
    // список слов, которые точно не являются прилагательными. собран по ОКПДТР.
    // TODO: temporal solution!
    private static final Map<String, Collection<String>> DEFINITELY_NOT_ADJECTIVES = Map.ofEntries(
            of("вая", "трамвая"),
            of("пий", "фильмокопий"),
            of("рий", "аварий", "территорий"),
            of("сий", "профессий", "экскурсий", "эмульсий"),
            of("тий", "партий", "покрытий", "предприятий"),
            of("ций", "декораций", "коллекций", "композиций", "конструкций", "лоций", "металлоконструкций",
                    "организаций", "секций", "ситуаций", "станций", "электростанций"),
            of("бий", "пособий"),
            of("зой", "базой", "фильмобазой"),
            of("вий", "путешествий", "условий"),
            of("дий", "орудий"),
            of("кой", "аптекой", "библиотекой", "видеотекой", "выставкой", "диспетчерской",
                    "клиникой", "корректорской", "мастерской", "намоткой",
                    "парикмахерской", "пленкой", "площадкой", "подготовкой", "практикой",
                    "свалкой", "смолкой", "техникой", "установкой", "фильмотекой"),
            of("мой", "платформой"),
            of("ной", "заправочной", "костюмерной", "котельной",
                    "портной", "прачечной", "приемной", "процедурной", "резиной", "турбиной"),
            of("пой", "группой", "труппой"),
            of("рой", "аспирантурой", "геокамерой", "докторантурой",
                    "камерой", "кафедрой", "конторой", "ординатурой", "физкультурой"),
            of("лий", "изделий", "металлоизделий", "сетеизделий", "специзделий", "стеклоизделий"),
            of("той", "кислотой", "комнатой"),
            of("ний", "декалькоманий", "зданий", "излучений", "измерений", "испытаний", "исследований", "линий",
                    "месторождений", "оснований", "отделений", "отправлений",
                    "подразделений", "помещений", "поручений", "приспособлений", "произведений",
                    "расписаний", "растений", "соединений", "сооружений", "строений", "термсоединений", "учреждений"),
            of("вой", "буровой", "вентилевой", "верховой", "горновой", "дверевой", "душевой", "кладовой",
                    "люковой", "миксеровой", "печевой", "скиповой", "стволовой"),
            of("дой", "слюдой")
    );

    private static final Collection<String> DEFINITELY_NEUTER_NOUNS = Set.of("знания", "прения");

    private static final List<String> MALE_ADJECTIVE_ENDINGS = List.of("ий", "ый", "ой");
    private static final List<String> FEMALE_ADJECTIVE_ENDINGS = List.of("ая", "яя");
    private static final List<String> NEUTER_ADJECTIVE_ENDINGS = List.of("ое", "ее");

    private static final List<String> PLURAL_ENDINGS = List.of("ы", "и", "я", "а");

    private static Map.Entry<String, Set<String>> of(String key, String... values) {
        return Map.entry(key, Set.of(values));
    }

    /**
     * Answers {@code true} if the given {@code word} consists of russian letters and hyphen.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean isRuWord(String word) {
        if (word.isBlank()) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            int ch = word.charAt(i);
            if (PlainDictionary.ALL_CHARS_LOWERCASE.contains(ch) || PlainDictionary.ALL_CHARS_UPPERCASE.contains(ch)) {
                continue;
            }
            if (ch == '-') {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Determines whether the specified {@code word} can be a singular nominative adjective.
     *
     * @param word   {@code String} to test, in lower-case, not {@code null}
     * @param gender {@link Gender}
     * @return {@code boolean}
     */
    public static boolean canBeAdjective(String word, Gender gender) {
        if (gender == Gender.MALE) {
            return canBeMasculineAdjective(word);
        }
        if (gender == Gender.FEMALE) {
            return canBeFeminineAdjective(word);
        }
        if (gender == Gender.NEUTER) {
            return canBeSingularNominativeNeuterAdjective(word);
        }
        return false;
    }

    private static boolean canBeMasculineAdjective(String word) {
        return canBeSingularNominativeMasculineAdjective(word) && !canBeMasculineAdjectiveBasedSubstantiveNoun(word);
    }

    private static boolean canBeFeminineAdjective(String word) {
        return canBeSingularNominativeFeminineAdjective(word) && !canBeFeminineAdjectiveBasedSubstantiveNoun(word);
    }

    /**
     * Determines whether the specified {@code word} can be a singular nominative masculine adjective
     * (i.e. является ли слово {@code прилагательным в мужском роде, единственном числе и именительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeSingularNominativeMasculineAdjective(String word) {
        return hasMaleAdjectiveEnding(word) && canBeSingularNominativeAdjective(word);
    }

    /**
     * Determines whether the specified {@code word} can be a singular nominative neuter adjective.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeSingularNominativeNeuterAdjective(String word) {
        return hasNeuterAdjectiveEnding(word);
    }

    /**
     * Determines whether the specified {@code word} can be a singular nominative feminine adjective
     * (i.e. является ли слово {@code прилагательным в женском роде, единственном числе и именительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeSingularNominativeFeminineAdjective(String word) {
        return hasFemaleAdjectiveEnding(word) && canBeSingularNominativeAdjective(word);
    }

    private static boolean hasMaleAdjectiveEnding(String word) {
        return TextUtils.endsWithOneOfIgnoreCase(word, MALE_ADJECTIVE_ENDINGS);
    }

    private static boolean hasFemaleAdjectiveEnding(String word) {
        return TextUtils.endsWithOneOfIgnoreCase(word, FEMALE_ADJECTIVE_ENDINGS);
    }

    private static boolean hasNeuterAdjectiveEnding(String word) {
        return TextUtils.endsWithOneOfIgnoreCase(word, NEUTER_ADJECTIVE_ENDINGS);
    }

    private static boolean canBeSingularNominativeAdjective(String word) {
        return word.length() > 2 && !DEFINITELY_NOT_ADJECTIVES
                .getOrDefault(word.substring(word.length() - 3), Collections.emptySet()).contains(word);
    }

    /**
     * Determines whether the specified {@code word} can be a noun-substantive from a masculine adjective
     * (i.e. является ли слово {@code существительным-субстантиватом из прилагательного в мужском роде}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeMasculineAdjectiveBasedSubstantiveNoun(String word) {
        return PlainDictionary.MASCULINE_SUBSTANTIVE_NOUNS.contains(TextUtils.normalize(word));
    }

    /**
     * Determines whether the given {@code word} can be a noun-substantive from a feminine adjective
     * (i.e. является ли слово {@code существительным-субстантиватом из прилагательного в женском роде}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineAdjectiveBasedSubstantiveNoun(String word) {
        return PlainDictionary.FEMININE_SUBSTANTIVE_NOUNS.contains(TextUtils.normalize(word));
    }

    // TODO: Remove NO_SUBSTANTIVE_PHRASE dic
    public static boolean canBePhraseIsNotSubstantiveNoun(String phrase) {
        return PlainDictionary.NO_SUBSTANTIVE_PHRASE.contains(TextUtils.normalize(phrase));
    }

    /**
     * Determines (not very accurately) whether the given {@code word} can be a singular and nominative feminine noun
     * (i.e. является ли слово {@code существительным в женском роде единственном числе и именительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineNoun(String word) {
        if (canBeFeminineAdjectiveBasedSubstantiveNoun(word)) {
            return true;
        }
        // свинья, ладья, свекла, берёза, копейка
        if (TextUtils.endsWithOneOfIgnoreCase(word, endings(Gender.FEMALE))) {
            return true;
        }
        String nw = TextUtils.normalize(word);
        if (DEFINITELY_NEUTER_NOUNS.contains(nw)) {
            return false;
        }
        // most of the nouns with ending '-ия' are female (see nouns.csv)
        return nw.endsWith("ия");
    }

    /**
     * Determines (not very accurately) whether the given {@code word} can be a singular and nominative neuter noun
     * (i.e. является ли слово {@code существительным в среднем роде единственном числе и именительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeNeuterNoun(String word) {
        if (DEFINITELY_NEUTER_NOUNS.contains(TextUtils.normalize(word))) {
            return true;
        }
        // солнце, облако, дерево
        return TextUtils.endsWithOneOfIgnoreCase(word, endings(Gender.NEUTER));
    }

    /**
     * Determines (not very accurately) whether the given {@code word} can be a singular and nominative masculine noun
     * (i.e. является ли слово {@code существительным в мужском роде единственном числе и именительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeMaleNoun(String word) {
        if (canBeMasculineAdjectiveBasedSubstantiveNoun(word)) {
            return true;
        }
        return TextUtils.endsWithOneOfIgnoreCase(word, endings(Gender.MALE));
    }

    private static Collection<String> endings(Gender gender) {
        return Objects.requireNonNull(PlainDictionary.NOUN_ENDINGS.get(gender.name().toLowerCase()));
    }

    /**
     * Determines (not very accurately) whether the given {@code noun} can be a singular and nominative neuter noun
     *
     * @param noun, a noun in nominative case, not {@code null}
     * @return {@code boolean}
     * @see #toPluralNoun(String)
     */
    public static boolean canBePlural(String noun) {
        // клиенты, сделки, сиделки, моряки, доллары, берёзы
        return TextUtils.endsWithOneOfIgnoreCase(noun, PLURAL_ENDINGS);
    }

    /**
     * Returns guessed gender of the specified noun (not accurate).
     *
     * @param singular {@code String}, a singular noun in nominative case, not {@code null}
     * @return {@link Gender} or {@code null}
     */
    public static Gender guessGenderOfSingularNoun(String singular) {
        String nw = TextUtils.normalize(singular);
        if (canBeNeuterNoun(nw)) { // солнце, облако, дерево
            return Gender.NEUTER;
        }
        if (canBeFeminineNoun(singular)) { // свинья, ладья, свекла, берёза, копейка
            return Gender.FEMALE;
        }
        if (canBeMaleNoun(singular)) {
            return Gender.MALE;
        }
        return null;
    }

    /**
     * Returns the gender of the specified adjective.
     *
     * @param word {@code String}, not {@code null}
     * @return {@link Gender} or {@code null}
     */
    public static Gender guessGenderOfSingleAdjective(String word) {
        if (hasFemaleAdjectiveEnding(word)) { // кривая, первая
            return Gender.FEMALE;
        }
        if (hasNeuterAdjectiveEnding(word)) { // второе
            return Gender.NEUTER;
        }
        if (hasMaleAdjectiveEnding(word)) { // нулевой
            return Gender.MALE;
        }
        return null;
    }

    /**
     * Attempts to create plural form from the specified singular.
     *
     * @param singular {@code String}, a singular noun in nominative case, not {@code null}
     * @return {@code String}
     */
    public static String toPluralNoun(String singular) {
        // TODO: make a rule for singular -> plural
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("ль", "ья", "ка", "ия"))) {
            // корабль,рубль,свинья,ладья,копейка,сделка,сиделка,инвестиция
            return TextUtils.replaceEnd(singular, 1, "и");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("ие"))) {
            // решение, отношение
            return TextUtils.replaceEnd(singular, 1, "я");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("ла", "за", "на"))) { // свекла,берёза,коза,старшина
            return TextUtils.replaceEnd(singular, 1, "ы");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("т", "р"))) { // цент,фунт,клиент,брезент,доллар,солдат
            return TextUtils.appendEnd(singular, "ы");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("к", "г"))) { // моряк, залог
            return TextUtils.appendEnd(singular, "и");
        }
        // TODO: complete
        return singular;
    }

    /**
     * Attempts to create singular form from the specified plural
     *
     * @param plural {@code String}, a plural noun in nominative case, not {@code null}
     * @return {@code String}
     */
    public static String toSingular(String plural) {
        // TODO: make a rule for plural -> singular
        if (TextUtils.endsWithIgnoreCase(plural, "ли")) { // корабли, рубли
            return TextUtils.replaceEnd(plural, 1, "ь");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("ия"))) { // состояния, действия
            return TextUtils.replaceEnd(plural, 1, "е");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("ьи", "ии"))) { // свиньи, ладьи, инвестиции
            return TextUtils.replaceEnd(plural, 1, "я");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("ки"))) { // копейки, сделки
            return TextUtils.replaceEnd(plural, 1, "а");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("лы", "зы"))) { // свеклы, берёзы
            return TextUtils.replaceEnd(plural, 1, "а");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("ты", "ры", "ки", "ги"))) {
            // центы,фунты,брезенты,доллары,залоги,моряки
            return plural.substring(0, plural.length() - 1);
        }
        // TODO: complete
        return plural;
    }

    public static String toSingularMasculineAdjective(String word) {
        if (TextUtils.endsWithOneOfIgnoreCase(word, List.of("ья", "ье", "ьи"))) { // кошачий, божий, птичий
            return TextUtils.replaceEnd(word, 2, "ий");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(word, List.of("ая", "ое"))) { // Хороший, Смешной
            return TextUtils.replaceEnd(word, 2, "ий") + TextUtils.replaceEnd(word, 2, "ой");
        }
        if (TextUtils.endsWithOneOfIgnoreCase(word, List.of("ее"))) { // Хороший
            return TextUtils.replaceEnd(word, 2, "ий");
        }
        // TODO: complete
        return word;
    }

    static boolean endsWithWord(String phrase, String word) {
        return word.equals(phrase) || phrase.endsWith(" " + word);
    }

    public static <X> X select(X female, X neuter, X male, Gender gender) {
        if (Gender.FEMALE == gender) {
            return female;
        }
        if (Gender.NEUTER == gender) {
            return neuter;
        }
        return male;
    }

    /**
     * Answers {@code true} if the given {@code word} can be abbreviation.
     *
     * @param word   {@code String} to test, not {@code null}
     * @param phrase the whole phrase containing the given {@code word}, or {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeAbbreviation(String word, String phrase) {
        String nw = TextUtils.normalize(word);
        if (PlainDictionary.ABBREVIATIONS.contains(nw)) {
            return true;
        }
        if (nw.length() > 1 &&
                nw.chars().allMatch(PlainDictionary.CONSONANT_CHARS_LOWERCASE::contains) ||
                nw.chars().allMatch(PlainDictionary.VOWEL_CHARS_LOWERCASE::contains)) {
            // probably abbreviation if only vowels or consonants
            return true;
        }
        // the given word is in upper-case but there is also lower-case chars
        return phrase != null && TextUtils.isUpperCase(word) && TextUtils.isMixedCase(phrase);
    }

    /**
     * Answers {@code true} if the given {@code word} can be abbreviation related to human.
     *
     * @param w {@code String}
     * @return {@code boolean}
     */
    public static boolean canBeHumanRelatedAbbreviation(String w) {
        for (String a : new String[]{"ип", "чп", "пбоюл"}) {
            if (a.equalsIgnoreCase(w)) {
                return true;
            }
        }
        return false;
    }

}

package pro.greendata.rugrammartools.impl.utils;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.dictionaries.PlainDictionary;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utilities for working with the Russian language, based on jobs-register (ОКПДТР, ~{@code 7860} records).
 * The rules were collected empirically.
 * <p>
 * Created by @ssz on 01.12.2020.
 */
public class GrammarUtils {

    private static final Collection<Integer> VOWEL_CHARS = "ауоыиэяюёе"
            .chars().boxed().collect(Collectors.toUnmodifiableSet());
    private static final Collection<Integer> CONSONANT_CHARS = "бвгджзйклмнпрстфхцчшщ"
            .chars().boxed().collect(Collectors.toUnmodifiableSet());

    // collection of simple prepositions
    private static final Collection<String> NON_DERIVATIVE_PREPOSITION = Set.of(
            "без", "в", "для", "до", "за", "из", "к", "на", "над", "о", "об", "от", "перед", "по", "под", "при", "про", "с", "у", "через"
    );

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

    private static final Set<String> FEMALE_NUMERALS = Set.of("одна", "две", "тысяча", "тысяч", "тысячи", "целая");
    private static final Set<String> MALE_NUMERALS = Set.of("один");

    private static final List<String> MALE_ADJECTIVE_ENDINGS = List.of("ий", "ый", "ой");
    private static final List<String> FEMALE_ADJECTIVE_ENDINGS = List.of("ая", "яя", "ка");
    private static final List<String> NEUTER_ADJECTIVE_ENDINGS = List.of("ое");

    private static final List<String> NEUTER_NOUN_ENDINGS = List.of("о", "е");
    private static final List<String> FEMALE_NOUN_ENDINGS = List.of("ья", "ла", "за", "ка");

    private static final List<String> ORDINAL_NUMERAL_ENDINGS = List.of("ой", "ый", "ий", "ая", "ое");

    private static final List<String> PLURAL_ENDINGS = List.of("ы", "и");

    private static Map.Entry<String, Set<String>> of(String key, String... values) {
        return Map.entry(key, Set.of(values));
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
        return canBeSingularNominativeMasculineAdjective(word)
                && !canBeMasculineAdjectiveBasedSubstantivatNoun(word);
    }

    private static boolean canBeFeminineAdjective(String word) {
        return canBeSingularNominativeFeminineAdjective(word)
                && !canBeFeminineAdjectiveBasedSubstantivatNoun(word);
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
    public static boolean canBeMasculineAdjectiveBasedSubstantivatNoun(String word) {
        return PlainDictionary.MASCULINE_SUBSTANTIVAT_NOUNS.contains(TextUtils.normalize(word, Dictionary.LOCALE));
    }

    /**
     * Determines whether the given {@code word} can be a noun-substantive from a feminine adjective
     * (i.e. является ли слово {@code существительным-субстантиватом из прилагательного в женском роде}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineAdjectiveBasedSubstantivatNoun(String word) {
        return PlainDictionary.FEMININE_SUBSTANTIVAT_NOUNS.contains(TextUtils.normalize(word, Dictionary.LOCALE));
    }

    /**
     * Determines (not very accurately) whether the given {@code word} can be a singular and nominative feminine noun
     * (i.e. является ли слово {@code существительным в женском роде единственном числе и именительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineNoun(String word) {
        if (canBeFeminineAdjectiveBasedSubstantivatNoun(word)) {
            return true;
        }
        // свинья, ладья, свекла, берёза, копейка
        return TextUtils.endsWithOneOfIgnoreCase(word, FEMALE_NOUN_ENDINGS);
    }

    /**
     * Determines (not very accurately) whether the given {@code word} can be a singular and nominative neuter noun
     * (i.e. является ли слово {@code существительным в среднем роде единственном числе и именительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeNeuterNoun(String word) {
        // солнце, облако, дерево
        return TextUtils.endsWithOneOfIgnoreCase(word, NEUTER_NOUN_ENDINGS);
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
     * Determines (quite accurately) whether the given {@code word}
     * can be a non-derivative preposition (i.e. является ли слово {@code непроизводным предлогом}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     * @see <a href='https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%B5%D0%B4%D0%BB%D0%BE%D0%B3'>Предлог</a>
     */
    public static boolean isNonDerivativePreposition(String word) {
        return NON_DERIVATIVE_PREPOSITION.contains(TextUtils.normalize(word, Dictionary.LOCALE));
    }

    /**
     * Determines whether the given {@code word} can be an ordinal numeral.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeOrdinalNumeral(String word) {
        return TextUtils.endsWithOneOfIgnoreCase(word, ORDINAL_NUMERAL_ENDINGS) && !isFractionNumeral(word);
    }

    /**
     * Returns guessed gender of the specified noun (not accurate).
     *
     * @param singular {@code String}, a singular noun in nominative case, not {@code null}
     * @return {@link Gender}
     */
    public static Gender guessGenderOfSingularNoun(String singular) {
        String nw = TextUtils.normalize(singular, Dictionary.LOCALE);
        if (canBeNeuterNoun(nw)) { // солнце, облако, дерево
            return Gender.NEUTER;
        }
        if (GrammarUtils.canBeFeminineNoun(singular)) { // свинья, ладья, свекла, берёза, копейка
            return Gender.FEMALE;
        }
        // the masculine gender is most common (in russian job-titles)
        return Gender.MALE;
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
     * Returns the gender of the specified numeral.
     *
     * @param standaloneNumeral {@code String}, not {@code null}
     * @return {@link Gender}
     */
    public static Gender guessGenderOfSingleNumeral(String standaloneNumeral) {
        String word = TextUtils.normalize(standaloneNumeral, Dictionary.LOCALE);
        if (MALE_NUMERALS.contains(word) || word.endsWith("ый")) { // один, десятый
            return Gender.MALE;
        }
        if (FEMALE_NUMERALS.contains(word) || word.endsWith("ая")) { // одна, десятая, сотая
            return Gender.FEMALE;
        }
        return Gender.NEUTER;
    }

    /**
     * Attempts to create plural form from the specified singular.
     *
     * @param singular {@code String}, a singular noun in nominative case, not {@code null}
     * @return {@code String}
     */
    public static String toPluralNoun(String singular) {
        // TODO: make a rule for singular -> plural
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("ль", "ья", "ка"))) {
            // корабль,рубль,свинья,ладья,копейка,сделка,сиделка
            return TextUtils.replaceEnd(singular, 1, "и", Dictionary.LOCALE);
        }
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("ла", "за", "на"))) { // свекла,берёза,коза,старшина
            return TextUtils.replaceEnd(singular, 1, "ы", Dictionary.LOCALE);
        }
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("т", "р"))) { // цент,фунт,клиент,брезент,доллар,солдат
            return TextUtils.appendEnd(singular, "ы", Dictionary.LOCALE);
        }
        if (TextUtils.endsWithOneOfIgnoreCase(singular, List.of("к", "г"))) { // моряк, залог
            return TextUtils.appendEnd(singular, "и", Dictionary.LOCALE);
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
            return TextUtils.replaceEnd(plural, 1, "ь", Dictionary.LOCALE);
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("ьи"))) { // свиньи, ладьи
            return TextUtils.replaceEnd(plural, 1, "я", Dictionary.LOCALE);
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("ки"))) { // копейки, сделки
            return TextUtils.replaceEnd(plural, 1, "а", Dictionary.LOCALE);
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("лы", "зы"))) { // свеклы, берёзы
            return TextUtils.replaceEnd(plural, 1, "а", Dictionary.LOCALE);
        }
        if (TextUtils.endsWithOneOfIgnoreCase(plural, List.of("ты", "ры", "ки", "ги"))) {
            // центы,фунты,брезенты,доллары,залоги,моряки
            return plural.substring(0, plural.length() - 1);
        }
        // TODO: complete
        return plural;
    }

    /**
     * Changes the gender of the specified cardinal numeral word.
     *
     * @param numeral {@code String}, a cardinal numeral in nominative case, not {@code null}
     * @param gender  {@link Gender}, not {@code null}
     * @return {@code String}
     */
    public static String changeGenderOfCardinalNumeral(String numeral, Gender gender) {
        String nw = TextUtils.normalize(numeral, Dictionary.LOCALE);
        switch (nw) {
            case "один":
                return TextUtils.toProperCase(numeral, select("одна", "одно", "один", gender));
            case "два":
                return TextUtils.toProperCase(numeral, select("две", "два", "два", gender));
        }
        return numeral;
    }

    /**
     * Changes the gender of the specified ordinal numeral word.
     *
     * @param numeral {@code String}, an ordinal numeral in nominative case, not {@code null}
     * @param gender  {@link Gender}, not {@code null}
     * @return {@code String}
     */
    public static String changeGenderOfOrdinalNumeral(String numeral, Gender gender) {
        String nw = TextUtils.normalize(numeral, Dictionary.LOCALE);
        if ("третий".equals(nw)) {
            return TextUtils.toProperCase(numeral, select("третья", "третье", "третий", gender));
        }
        // одиннадцатый одиннадцатое одиннадцатая
        // четвёртый четвёртое четвёртая
        // седьмой седьмое седьмая
        String ending = select("ая", "ое", nw.endsWith("ый") ? "ый" : "ой", gender);
        String res = TextUtils.replaceEnd(nw, 2, ending, Dictionary.LOCALE);
        return TextUtils.toProperCase(numeral, res);
    }

    public static boolean isFractionNumeral(String number) {
        number = TextUtils.normalize(number, Dictionary.LOCALE);
        return number.contains(" целых ") || number.contains("одна целая ");
    }

    public static boolean isNumeralEndWithNumberOne(String number) {
        return endsWithWord(TextUtils.normalize(number, Dictionary.LOCALE), "один");
    }

    public static boolean isNumeralEndWithTwoThreeFour(String number) {
        number = TextUtils.normalize(number, Dictionary.LOCALE);
        return endsWithWord(number, "два") || endsWithWord(number, "три") || endsWithWord(number, "четыре");
    }

    public static boolean isZeroNumeral(String number) {
        number = TextUtils.normalize(number, Dictionary.LOCALE);
        return "ноль".equals(number);
    }

    private static boolean endsWithWord(String phrase, String word) {
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
        String nw = TextUtils.normalize(word, Dictionary.LOCALE);
        if (PlainDictionary.ABBREVIATIONS.contains(nw)) {
            return true;
        }
        if (nw.length() > 1 && nw.chars().allMatch(CONSONANT_CHARS::contains) || nw.chars().allMatch(VOWEL_CHARS::contains)) {
            // probably abbreviation if only vowels or consonants
            return true;
        }
        // the given word is in upper-case but there is also lower-case chars
        return phrase != null && TextUtils.isUpperCase(word) && TextUtils.isMixedCase(phrase);
    }

    /**
     * Answers {@code true} if the given {@code word} can be abbreviation related to human.
     * @param w {@code String}
     * @return {@code boolean}
     */
    public static boolean canBeHumanRelatedAbbreviation(String w) {
        for (String a : new String[]{"ип", "чп", "пбоюл"})  {
            if (a.equalsIgnoreCase(w)) {
                return true;
            }
        }
        return false;
    }
}

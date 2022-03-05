package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;

import java.util.*;

/**
 * Utilities for working with the Russian language, based on jobs-register (ОКПДТР, ~{@code 7860} records).
 * The rules were collected empirically.
 * <p>
 * Created by @ssz on 01.12.2020.
 */
public class GrammarUtils {

    // collection of substantive feminine nouns that look like adjectives
    // (субстантивные существительные женского рода, которые выглядят как прилагательные)
    private static final Collection<String> FEMININE_SUBSTANTIVAT_NOUNS = Set.of(
            "буровая",
            "горничная",
            "заведующая",
            "заправочная"
    );
    // collection of substantive masculine nouns that look like adjectives
    // (субстантивные существительные мужского рода, которые выглядят как прилагательные)
    private static final Collection<String> MASCULINE_SUBSTANTIVAT_NOUNS = Set.of(
            "вентилевой", "верховой", "выпускающий",
            "горновой", "горнорабочий",
            "дверевой", "дежурный", "дневальный",
            "заведующий",
            "лесничий", "люковой",
            "миксеровой",
            "печевой", "поверенный", "подручный", "пожарный", "портной",
            "рабочий",
            "торфорабочий",
            "уполномоченный", "управляющий" // "ученый" как прилагательное ("ученый секретарь")
    );
    // collection simple prepositions
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

    private static final List<String> ORDINAL_NUMERAL_ENDINGS = List.of("ой", "ый", "ая", "ое");

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
        for (String end : MALE_ADJECTIVE_ENDINGS) {
            if (MiscStringUtils.endsWithIgnoreCase(word, end)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFemaleAdjectiveEnding(String word) {
        for (String end : FEMALE_ADJECTIVE_ENDINGS) {
            if (MiscStringUtils.endsWithIgnoreCase(word, end)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNeuterAdjectiveEnding(String word) {
        for (String end : NEUTER_ADJECTIVE_ENDINGS) {
            if (MiscStringUtils.endsWithIgnoreCase(word, end)) {
                return true;
            }
        }
        return false;
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
        return MASCULINE_SUBSTANTIVAT_NOUNS.contains(MiscStringUtils.normalize(word, Dictionary.LOCALE));
    }

    /**
     * Determines whether the given {@code word} can be a noun-substantive from a feminine adjective
     * (i.e. является ли слово {@code существительным-субстантиватом из прилагательного в женском роде}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineAdjectiveBasedSubstantivatNoun(String word) {
        return FEMININE_SUBSTANTIVAT_NOUNS.contains(MiscStringUtils.normalize(word, Dictionary.LOCALE));
    }

    /**
     * Determines (not very accurately) whether the given {@code word} can be a singular and nominative feminine noun
     * (i.e. является ли слово {@code существительным в женском роде единственном числе и имменительном падеже}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineNoun(String word) {
        return canBeFeminineAdjectiveBasedSubstantivatNoun(word);
    }

    /**
     * Determines (quite accurately) whether the given {@code word}
     * can be a non-derivative preposition (i.e. является ли слово {@code непроизводным предлогом}?).
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     * @see <a href='https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%B5%D0%B4%D0%BB%D0%BE%D0%B3'>Предлог</a>
     */
    public static boolean canBeNonDerivativePreposition(String word) {
        return NON_DERIVATIVE_PREPOSITION.contains(MiscStringUtils.normalize(word, Dictionary.LOCALE));
    }

    public static boolean canBeOrdinalNumeral(String word) {
        for (String e : ORDINAL_NUMERAL_ENDINGS) {
            if (MiscStringUtils.endsWithIgnoreCase(word, e)) {
                return !isFractionNumeral(word);
            }
        }
        return false;
    }

    /**
     * Returns the gender of the specified noun (not accurate).
     *
     * @param singular {@code String}, a singular noun in nominative case, not {@code null}
     * @return {@link Gender}
     */
    public static Gender guessGenderOfSingularNoun(String singular) {
        String nw = MiscStringUtils.normalize(singular, Dictionary.LOCALE);
        if (nw.endsWith("ья") || nw.endsWith("ла") || nw.endsWith("за") || nw.endsWith("ка")) {
            // свинья, ладья, свекла, берёза, копейка
            return Gender.FEMALE;
        }
        // TODO: complete
        // most common gender
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
        String word = MiscStringUtils.normalize(standaloneNumeral, Dictionary.LOCALE);
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
        String nw = MiscStringUtils.normalize(singular, Dictionary.LOCALE);
        if (nw.endsWith("ль")) { // корабль, рубль
            return MiscStringUtils.replaceEnd(singular, 2, "ли", Dictionary.LOCALE);
        }
        if (nw.endsWith("ья") || nw.endsWith("ка")) { // свинья, ладья, копейка
            return MiscStringUtils.replaceEnd(singular, 1, "и", Dictionary.LOCALE);
        }
        if (nw.endsWith("ла") || nw.endsWith("за")) { // свекла, берёзы
            return MiscStringUtils.replaceEnd(singular, 1, "ы", Dictionary.LOCALE);
        }
        if (nw.endsWith("нт") || nw.endsWith("р")) { // цент,фунт,брезент,доллар
            return MiscStringUtils.appendEnd(singular, "ы", Dictionary.LOCALE);
        }
        // TODO: complete
        return singular;
    }

    /**
     * Tries to determine correct gender form of the specified numeral word.
     *
     * @param neuterNumeral {@code String}, a numeral in nominative case, not {@code null}
     * @param gender        {@link Gender}
     * @return {@code String}
     */
    public static String changeGenderFormOfNumeral(String neuterNumeral, Gender gender) {
        String nw = MiscStringUtils.normalize(neuterNumeral, Dictionary.LOCALE);
        if (gender == Gender.FEMALE) {
            switch (nw) {
                case "один":
                    return MiscStringUtils.toProperCase(neuterNumeral, "одна");
                case "два":
                    return MiscStringUtils.toProperCase(neuterNumeral, "две");
            }
        }
        return neuterNumeral;
    }

    public static boolean isFractionNumeral(String number) {
        number = MiscStringUtils.normalize(number, Dictionary.LOCALE);
        return number.contains(" целых ") || number.contains("одна целая ");
    }

    public static boolean isNumeralEndWithNumberOne(String number) {
        return endsWithWord(MiscStringUtils.normalize(number, Dictionary.LOCALE), "один");
    }

    public static boolean isNumeralEndWithTwoThreeFour(String number) {
        number = MiscStringUtils.normalize(number, Dictionary.LOCALE);
        return endsWithWord(number, "два") || endsWithWord(number, "три") || endsWithWord(number, "четыре");
    }

    public static boolean isZeroNumeral(String number) {
        number = MiscStringUtils.normalize(number, Dictionary.LOCALE);
        return "ноль".equals(number);
    }

    private static boolean endsWithWord(String phrase, String word) {
        return word.equals(phrase) || phrase.endsWith(" " + word);
    }

}
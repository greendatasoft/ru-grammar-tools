package com.gitlab.sszuev.inflector.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for working with the Russian language, based on jobs-register (ОКПДТР, ~{@code 7860} records).
 * The rules were collected empirically.
 * <p>
 * Created by @ssz on 01.12.2020.
 */
public class GrammarUtils {

    // список субстантивных существительных женского рода, которые выглядят как прилагательные
    private static final Collection<String> FEMININE_SUBSTANTIVAT_NOUNS = of(
            "буровая",
            "горничная",
            "заведующая",
            "заправочная"
    );
    // список субстантивных существительных мужского рода, которые выглядят как прилагательные
    private static final Collection<String> MASCULINE_SUBSTANTIVAT_NOUNS = of(
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
    // список простых предлогов
    private static final Collection<String> NON_DERIVATIVE_PREPOSITION = of(
            "без", "в", "для", "до", "за", "из", "к", "на", "над", "о", "об", "от", "перед", "по", "под", "при", "про", "с", "у", "через"
    );

    // список слов, которые точно не являются прилагательными. собран по ОКПДТР.
    // TODO: временное решение!
    private static final Map<String, Collection<String>> DEFINITELY_NOT_ADJECTIVES = new HashMap<String, Collection<String>>() {
        {
            put("вая", of("трамвая"));
            put("пий", of("фильмокопий"));
            put("рий", of("аварий", "территорий"));
            put("сий", of("профессий", "экскурсий", "эмульсий"));
            put("тий", of("партий", "покрытий", "предприятий"));
            put("ций", of("декораций", "коллекций", "композиций", "конструкций", "лоций", "металлоконструкций",
                    "организаций", "секций", "ситуаций", "станций", "электростанций"));
            put("бий", of("пособий"));
            put("зой", of("базой", "фильмобазой"));
            put("вий", of("путешествий", "условий"));
            put("дий", of("орудий"));
            put("кой", of("аптекой", "библиотекой", "видеотекой", "выставкой", "диспетчерской",
                    "клиникой", "корректорской", "мастерской", "намоткой",
                    "парикмахерской", "пленкой", "площадкой", "подготовкой", "практикой",
                    "свалкой", "смолкой", "техникой", "установкой", "фильмотекой"));
            put("мой", of("платформой"));
            put("ной", of("заправочной", "костюмерной", "котельной",
                    "портной", "прачечной", "приемной", "процедурной", "резиной", "турбиной"));
            put("пой", of("группой", "труппой"));
            put("рой", of("аспирантурой", "геокамерой", "докторантурой",
                    "камерой", "кафедрой", "конторой", "ординатурой", "физкультурой"));
            put("лий", of("изделий", "металлоизделий", "сетеизделий", "специзделий", "стеклоизделий"));
            put("той", of("кислотой", "комнатой"));
            put("ний", of("декалькоманий", "зданий", "излучений", "измерений", "испытаний", "исследований", "линий",
                    "месторождений", "оснований", "отделений", "отправлений",
                    "подразделений", "помещений", "поручений", "приспособлений", "произведений",
                    "расписаний", "растений", "соединений", "сооружений", "строений", "термсоединений", "учреждений"));
            put("вой", of("буровой", "вентилевой", "верховой", "горновой", "дверевой", "душевой", "кладовой",
                    "люковой", "миксеровой", "печевой", "скиповой", "стволовой"));
            put("дой", of("слюдой"));
        }
    };

    /**
     * Определяет, может ли переданное слово ({@code word}) быть прилагательным в мужском роде,
     * единственном числе и именительном падеже.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeSingularNominativeMasculineAdjective(String word) {
        return Stream.of("ий", "ый", "ой").anyMatch(word::endsWith) && canBeSingularNominativeAdjective(word);
    }

    /**
     * Определяет, может ли переданное слово ({@code word}) быть прилагательным в женском роде,
     * единственном числе и именительном падеже.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeSingularNominativeFeminineAdjective(String word) {
        return Stream.of("ая", "яя").anyMatch(word::endsWith) && canBeSingularNominativeAdjective(word);
    }

    private static boolean canBeSingularNominativeAdjective(String word) {
        return word.length() > 2 && !DEFINITELY_NOT_ADJECTIVES
                .getOrDefault(word.substring(word.length() - 3), Collections.emptySet()).contains(word);
    }

    /**
     * Определяет, может ли переданное слово ({@code word}) быть
     * существительным-субстантиватом из прилагательного в мужском роде.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeMasculineAdjectiveBasedSubstantivatNoun(String word) {
        return MASCULINE_SUBSTANTIVAT_NOUNS.contains(normalize(word));
    }

    /**
     * Определяет, может ли переданное слово ({@code word}) быть
     * существительным-субстантиватом из прилагательного в женском роде.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineAdjectiveBasedSubstantivatNoun(String word) {
        return FEMININE_SUBSTANTIVAT_NOUNS.contains(normalize(word));
    }

    /**
     * Определяет (неточно), может ли переданное слово ({@code word}) быть существительным в женском роде,
     * единственном числе и имменительным падеже.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     */
    public static boolean canBeFeminineNoun(String word) {
        return canBeFeminineAdjectiveBasedSubstantivatNoun(word);
    }

    /**
     * Определяет (достаточно точно), может ли переданное слово ({@code word}) быть непроизводным предлогом.
     *
     * @param word {@code String}, not {@code null}
     * @return {@code boolean}
     * @see <a href='https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%B5%D0%B4%D0%BB%D0%BE%D0%B3'>Предлог</a>
     */
    public static boolean canBeNonDerivativePreposition(String word) {
        return NON_DERIVATIVE_PREPOSITION.contains(normalize(word));
    }

    private static String normalize(String s) {
        return s.trim().toLowerCase();
    }

    @SafeVarargs
    private static <X> Set<X> of(X... values) {
        return Arrays.stream(values).collect(Collectors.toSet());
    }
}

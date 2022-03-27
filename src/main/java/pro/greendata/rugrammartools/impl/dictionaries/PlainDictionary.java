package pro.greendata.rugrammartools.impl.dictionaries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A plain dictionary that contains only flat {@code Collection}s.
 * <p>
 * Created by @ssz on 28.02.2022.
 */
public class PlainDictionary {
    public static final Collection<String> FEMALE_NAMES = load("/female-names.txt");
    public static final Collection<String> MALE_NAMES = load("/male-names.txt");

    /**
     * true (indeclinable) abbreviations
     */
    public static final Collection<String> ABBREVIATIONS = load("/abbreviations.txt");
    /**
     * collection of substantive feminine nouns that look like adjectives
     * (субстантивные существительные женского рода, которые выглядят как прилагательные)
     */
    public static final Collection<String> FEMININE_SUBSTANTIVE_NOUNS = load("/female-substantives.txt");
    /**
     * collection of substantive masculine nouns that look like adjectives
     * (субстантивные существительные мужского рода, которые выглядят как прилагательные)
     */
    public static final Collection<String> MASCULINE_SUBSTANTIVE_NOUNS = load("/male-substantives.txt");
    /**
     * A {@code List} of big cardinal numerals, so called {@code "Короткая шкала"}.
     *
     * @see <a href='https://ru.wikipedia.org/wiki/%D0%98%D0%BC%D0%B5%D0%BD%D0%BD%D1%8B%D0%B5_%D0%BD%D0%B0%D0%B7%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F_%D1%81%D1%82%D0%B5%D0%BF%D0%B5%D0%BD%D0%B5%D0%B9_%D1%82%D1%8B%D1%81%D1%8F%D1%87%D0%B8'>Именные названия степеней тысячи</a>
     */
    public static final List<String> BIG_CARDINAL_NUMERALS = List.of(
            "тысяча", "миллион", "миллиард", "триллион", "квадриллион", "квинтиллион", "секстиллион", "септиллион",
            "октиллион", "нониллион", "дециллион", "ундециллион", "дуодециллион", "тредециллион", "кваттордециллион",
            "квиндециллион", "седециллион", "септдециллион", "октодециллион", "новемдециллион", "вигинтиллион");
    /**
     * A collection of simple (non-derivative) prepositions ({@code список непроизводных предлогов})
     * @see <a href='https://ru.wikipedia.org/wiki/%D0%9F%D1%80%D0%B5%D0%B4%D0%BB%D0%BE%D0%B3'>Предлог</a>
     */
    public static final Collection<String> NON_DERIVATIVE_PREPOSITION = Set.of(
            "без", "в", "для", "до", "за", "из", "к", "на", "над", "о", "об", "от", "перед", "по", "под", "при", "про", "с", "у", "через"
    );
    public static final Collection<Integer> VOWEL_CHARS_LOWERCASE = "ауоыиэяюёе"
            .chars().boxed().collect(Collectors.toUnmodifiableSet());
    public static final Collection<Integer> CONSONANT_CHARS_LOWERCASE = "бвгджзйклмнпрстфхцчшщ"
            .chars().boxed().collect(Collectors.toUnmodifiableSet());
    public static final Collection<Integer> ALL_CHARS_LOWERCASE = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            .chars().boxed().collect(Collectors.toUnmodifiableSet());
    public static final Collection<Integer> ALL_CHARS_UPPERCASE = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
            .chars().boxed().collect(Collectors.toUnmodifiableSet());

    /**
     * Collection of noun-endings.
     * Each ending belongs to one and only one gender group (ambiguous endings are excluded).
     * Collected from {@code nouns.csv}.
     */
    public static final Map<String, Collection<String>> NOUN_ENDINGS = loadAsMap("/noun-endings.txt");

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Collection<String>> loadAsMap(@SuppressWarnings("SameParameterValue") String resource) {
        Set<String> res = load(resource);
        Map.Entry[] values = res.stream().map(PlainDictionary::parseLine).toArray(Map.Entry[]::new);
        return Map.ofEntries(values);
    }

    private static Map.Entry<String, Collection<String>> parseLine(String line) {
        String[] kv = line.split(":\\s*");
        if (kv.length != 2) {
            throw new IllegalStateException("Wrong line: " + line);
        }
        Collection<String> value = Set.of(Arrays.stream(kv[1].split(",\\s*"))
                .map(String::trim).filter(x -> !x.isEmpty()).toArray(String[]::new));
        return Map.entry(kv[0].trim(), value);
    }

    private static Set<String> load(String resource) {
        try (InputStream in = Objects.requireNonNull(PlainDictionary.class.getResourceAsStream(resource));
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Stream<String> stream = reader.lines()) {
            return Set.of(stream.filter(x -> !skip(x)).map(String::trim).toArray(String[]::new));
        } catch (IOException e) {
            throw new IllegalStateException("Can't load " + resource, e);
        }
    }

    private static boolean skip(String s) {
        return s.isBlank() || s.startsWith("#");
    }
}

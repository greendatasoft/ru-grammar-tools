package pro.greendata.rugrammartools.impl.dictionaries;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.Word;
import pro.greendata.rugrammartools.impl.utils.MiscStringUtils;

import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A dictionary, it contains words from resources (csv-file).
 * Created by @ssz on 24.02.2022.
 *
 * @see <a href='https://github.com/Badestrand/russian-dictionary'>Russian Dictionary Data</a>
 */
public class Dictionary {
    public static final Locale LOCALE = new Locale("ru", "ru");

    private static final Dictionary NOUN_DICTIONARY = new Dictionary("/nouns.csv");

    private final Supplier<Map<String, Record>> loader;
    // it is okay to have Map in memory: it is not so big (~20_000 records (file size = 8MB),
    // but just in case store it as SoftReference:
    private volatile SoftReference<Map<String, Record>> content;

    protected Dictionary(String path) {
        Objects.requireNonNull(path);
        this.loader = () -> load(path);
    }

    /**
     * Returns a dictionary, that contains nouns.
     *
     * @return {@link Dictionary}
     */
    public static Dictionary getNounDictionary() {
        return NOUN_DICTIONARY;
    }

    /**
     * Loads the {@link Dictionary} from the file system.
     *
     * @param source {@code String} - resource
     * @return immutable {@code Map}
     */
    @SuppressWarnings({"unchecked"})
    protected static Map<String, Record> load(String source) {
        Map<String, Record> data = new HashMap<>(26900);
        try (InputStream in = Objects.requireNonNull(Dictionary.class.getResourceAsStream(source));
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Stream<String> lines = reader.lines()) {
            lines.forEach(record -> {
                Map.Entry<String, Record> e = WordRecord.parse(record);
                if (e == null) {
                    return;
                }
                data.merge(e.getKey(), e.getValue(), MultiRecord::create);
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Can't load " + source, e);
        }
        // MapN must be faster:
        Map.Entry<String, Record>[] array = data.entrySet().toArray(Map.Entry[]::new);
        return Map.ofEntries(array);
    }

    /**
     * Returns a word-info object.
     *
     * @param word {@code String}, not {@code null}
     * @return an {@code Optional} of {@link Word}
     */
    public Optional<Word> wordDetails(String word) {
        return wordDetails(word, null, null);
    }

    /**
     * Tries to find the most suitable word record object.
     *
     * @param word    {@code String}, the key, not {@code null}
     * @param gender  {@link Gender} a filter parameter, can be {@code null}
     * @param animate {@code Boolean}a filter parameter, can be {@code null}
     * @return an {@code Optional} of {@link Word}
     */
    public Optional<Word> wordDetails(String word, Gender gender, Boolean animate) {
        String key = MiscStringUtils.normalize(word, LOCALE);
        Record record = contentMap().get(key);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(selectSingleRecord(record, gender, animate));
    }

    /**
     * Tries best to select the most appropriate word record according to the specified parameters.
     *
     * @param record   {@link Record}
     * @param gender   {@code Gender}
     * @param animated {@code Boolean}
     * @return {@link WordRecord}
     */
    protected WordRecord selectSingleRecord(Record record, Gender gender, Boolean animated) {
        if (record instanceof WordRecord) {
            return (WordRecord) record;
        }
        MultiRecord multi = (MultiRecord) record;
        List<WordRecord> res = Arrays.stream(multi.words)
                .sorted(Comparator.comparingInt(WordRecord::fullness).reversed())
                .filter(s -> (gender == null || s.gender == gender) &&
                        (animated == null || s.animated == animated))
                .collect(Collectors.toList());
        if (res.isEmpty()) { // can't select, choose first
            return multi.words[0];
        }
        return res.get(0);
    }

    protected Map<String, Record> contentMap() {
        SoftReference<Map<String, Record>> content = this.content;
        Map<String, Record> res;
        if (content != null && (res = content.get()) != null) {
            return res;
        }
        synchronized (this) {
            content = this.content;
            if (content != null && (res = content.get()) != null) {
                return res;
            }
            this.content = new SoftReference<>(res = loader.get());
            return res;
        }
    }

    interface Record {
    }

    static class MultiRecord implements Record {
        private final WordRecord[] words;

        MultiRecord(WordRecord[] words) {
            this.words = words;
        }

        static Record create(Record... records) {
            List<WordRecord> res = new ArrayList<>();
            for (Record w : records) {
                if (w instanceof WordRecord) {
                    res.add((WordRecord) w);
                } else {
                    res.addAll(Arrays.asList(((MultiRecord) w).words));
                }
            }
            return new MultiRecord(res.toArray(WordRecord[]::new));
        }

        @Override
        public String toString() {
            return String.format("{%s}", Arrays.toString(words));
        }
    }

    public static class WordRecord implements Record, Word {
        private Gender gender;
        private Boolean animated;
        private Boolean indeclinable;
        private String plural;
        private String[] singularCases;
        private String[] pluralCases;

        /**
         * Parses the csv-line (for noun).
         * The header:
         * {@code bare,accented,translations_en,translations_de,gender,partner,animate,indeclinable,sg_only,pl_only,
         * sg_nom,sg_gen,sg_dat,sg_acc,sg_inst,sg_prep,pl_nom,pl_gen,pl_dat,pl_acc,pl_inst,pl_prep}
         *
         * @param sourceLine {@code String}
         * @return a {@code Map.Entry}
         */
        private static Map.Entry<String, Record> parse(String sourceLine) {
            String[] array = sourceLine.split("\t");
            String key = MiscStringUtils.normalize(Objects.requireNonNull(array[0]), LOCALE);
            if (array.length < 5) {
                return null;
            }
            WordRecord res = new WordRecord();
            res.gender = parseGender(array);
            if (array.length < 7) {
                return Map.entry(key, res);
            }
            res.animated = parseBoolean(array[6]);
            if (array.length < 8) {
                return Map.entry(key, res);
            }
            res.indeclinable = parseBoolean(array[7]);
            if (res.indeclinable == Boolean.TRUE) {
                return Map.entry(key, res);
            }
            if (array.length < 16) {
                return Map.entry(key, res);
            }
            res.singularCases = new String[5];
            for (int i = 0; i < 5; i++) {
                res.singularCases[i] = normalizeValue(array[11 + i]);
            }
            if (array.length < 22) {
                return Map.entry(key, res);
            }
            res.plural = normalizeValue(array[16]);
            res.pluralCases = new String[5];
            for (int i = 0; i < 5; i++) {
                res.pluralCases[i] = normalizeValue(array[17 + i]);
            }
            return Map.entry(key, res);
        }

        private static Gender parseGender(String[] array) {
            if ("f".equals(array[4])) {
                return Gender.FEMALE;
            }
            if ("n".equals(array[4])) {
                return Gender.NEUTER;
            }
            if ("m".equals(array[4])) {
                return Gender.MALE;
            }
            return null;
        }

        private static Boolean parseBoolean(String val) {
            if ("1".equals(val)) {
                return Boolean.TRUE;
            }
            if ("0".equals(val)) {
                return Boolean.FALSE;
            }
            return null;
        }

        private static String normalizeValue(String value) {
            return MiscStringUtils.normalize(value, LOCALE).replace("'", "");
        }

        @Override
        public Gender gender() {
            return gender;
        }

        @Override
        public Boolean animate() {
            return animated;
        }

        public String[] singularCases() {
            return singularCases;
        }

        public String plural() {
            return plural;
        }

        public String[] pluralCases() {
            return pluralCases;
        }

        @Override
        public boolean isIndeclinable() {
            return indeclinable != null && indeclinable;
        }

        /**
         * Answers a degree of fullness.
         *
         * @return {@code int}
         */
        public int fullness() {
            return Stream.of(gender, animated, indeclinable, plural, singularCases, pluralCases)
                    .filter(Objects::nonNull).mapToInt(x -> 1).sum();
        }

        @Override
        public String toString() {
            return String.format("Record{gender=%s, animated=%s, indeclinable=%s, plural='%s', singularCases=%s, pluralCases=%s}",
                    gender, animated, indeclinable, plural, Arrays.toString(singularCases), Arrays.toString(pluralCases));
        }
    }
}

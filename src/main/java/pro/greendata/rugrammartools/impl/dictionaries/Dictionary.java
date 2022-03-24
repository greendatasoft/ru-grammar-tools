package pro.greendata.rugrammartools.impl.dictionaries;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.Word;
import pro.greendata.rugrammartools.impl.utils.RuleUtils;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

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
        Map<Record, Record> cache = new HashMap<>(26900);
        try (InputStream in = Objects.requireNonNull(Dictionary.class.getResourceAsStream(source));
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Stream<String> lines = reader.lines()) {
            lines.forEach(record -> {
                Map.Entry<String, ? extends Record> e = WordRecord.parse(record);
                if (e == null) {
                    return;
                }
                Record value = cache.computeIfAbsent(e.getValue(), x -> e.getValue());
                data.merge(e.getKey(), value, MultiRecord::create);
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
     * @param key     {@code String}, the key (normalized: lowercase without trailing spaces), not {@code null}
     * @param gender  {@link Gender} a filter parameter, can be {@code null}
     * @param animate {@code Boolean}a filter parameter, can be {@code null}
     * @return an {@code Optional} of {@link Word}
     */
    public Optional<Word> wordDetails(String key, Gender gender, Boolean animate) {
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
                .filter(s -> (gender == null || s.gender() == gender) &&
                        (animated == null || s.animate() == animated))
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

    /**
     * A base dictionary record interface.
     */
    interface Record {
    }

    /**
     * Implementation that holds several {@link WordRecord}s.
     */
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

    /**
     * A record for noun (right now).
     */
    public static class WordRecord implements Record, Word {
        private static final int HAS_ANIMATE = 2;
        private static final int IS_ANIMATE = 4;
        private static final int HAS_INDECLINABLE = 8;
        private static final int IS_INDECLINABLE = 16;
        private static final int GENDER_FLAG_0 = 32;
        private static final int GENDER_FLAG_1 = 64;

        private int characteristics;
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
        private static Map.Entry<String, WordRecord> parse(String sourceLine) {
            String[] array = sourceLine.split("\t");
            String key = TextUtils.normalize(Objects.requireNonNull(array[0]));
            if (array.length < 5) {
                return null;
            }
            WordRecord res = new WordRecord();
            res.gender(parseGender(array[4]));
            if (array.length < 7) {
                return Map.entry(key, res);
            }
            res.animate(parseBoolean(array[6]));
            if (array.length < 8) {
                return Map.entry(key, res);
            }
            res.indeclinable(parseBoolean(array[7]));
            if (res.indeclinable() == Boolean.TRUE) {
                return Map.entry(key, res);
            }
            if (array.length < 16) {
                return Map.entry(key, res);
            }
            res.singularCases = new String[5];
            for (int i = 0; i < 5; i++) {
                res.singularCases[i] = toEnding(key, array[11 + i]);
            }
            if (array.length < 22) {
                return Map.entry(key, res);
            }
            res.plural = toEnding(key, array[16]);
            res.pluralCases = new String[5];
            for (int i = 0; i < 5; i++) { // note that the base here is a singular key, not plural its form
                res.pluralCases[i] = toEnding(key, array[17 + i]);
            }
            return Map.entry(key, res);
        }

        private static String toEnding(String key, String word) {
            String w = normalizeValue(word);
            if (!w.contains(",")) {
                return RuleUtils.calcEnding(key, w);
            }
            StringJoiner res = new StringJoiner(",");
            for (String p : w.split(",\\s*")) {
                res.add(RuleUtils.calcEnding(key, p));
            }
            return res.toString();
        }

        private static Gender parseGender(String g) {
            if ("f".equals(g)) {
                return Gender.FEMALE;
            }
            if ("n".equals(g)) {
                return Gender.NEUTER;
            }
            if ("m".equals(g)) {
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
            return TextUtils.normalize(value).replace("'", "");
        }

        @Override
        public Gender gender() {
            if (hasCharacteristics(GENDER_FLAG_0)) {
                return hasCharacteristics(GENDER_FLAG_1) ? Gender.MALE : Gender.FEMALE;
            }
            return hasCharacteristics(GENDER_FLAG_1) ? Gender.NEUTER : null;
        }

        private void gender(Gender g) {
            characteristicsOff(GENDER_FLAG_0);
            characteristicsOff(GENDER_FLAG_1);
            if (g == null) {
                return;
            }
            if (g == Gender.FEMALE) {
                characteristicsOn(GENDER_FLAG_0);
                return;
            }
            if (g == Gender.NEUTER) {
                characteristicsOn(GENDER_FLAG_1);
                return;
            }
            characteristicsOn(GENDER_FLAG_0);
            characteristicsOn(GENDER_FLAG_1);
        }

        @Override
        public Boolean animate() {
            return hasCharacteristics(HAS_ANIMATE, IS_ANIMATE);
        }

        private void animate(Boolean flag) {
            setCharacteristics(flag, HAS_ANIMATE, IS_ANIMATE);
        }

        protected Boolean indeclinable() {
            return hasCharacteristics(HAS_INDECLINABLE, IS_INDECLINABLE);
        }

        private void indeclinable(Boolean flag) {
            setCharacteristics(flag, HAS_INDECLINABLE, IS_INDECLINABLE);
        }

        private void setCharacteristics(Boolean flag, int has, int is) {
            if (flag == null) {
                characteristicsOff(has);
            } else {
                characteristicsOn(has);
                if (flag) {
                    characteristicsOn(is);
                } else if (hasCharacteristics(is)) {
                    characteristicsOff(is);
                }
            }
        }

        private Boolean hasCharacteristics(int has, int is) {
            return hasCharacteristics(has) ? hasCharacteristics(is) : null;
        }

        private boolean hasCharacteristics(int ch) {
            return (characteristics & ch) == ch;
        }

        private void characteristicsOn(int ch) {
            characteristics = characteristics | ch;
        }

        private void characteristicsOff(int ch) {
            if (!hasCharacteristics(ch)) {
                return;
            }
            characteristics = characteristics ^ ch;
        }

        @Override
        public String[] singularCases() {
            return singularCases;
        }

        @Override
        public String plural() {
            return plural;
        }

        @Override
        public String[] pluralCases() {
            return pluralCases;
        }

        @Override
        public boolean isIndeclinable() {
            return indeclinable() != null && indeclinable();
        }

        /**
         * Answers a degree of fullness.
         *
         * @return {@code int}
         */
        public int fullness() {
            return Stream.of(gender(), animate(), indeclinable(), plural, singularCases, pluralCases)
                    .filter(Objects::nonNull).mapToInt(x -> 1).sum();
        }

        @Override
        public String toString() {
            return String.format("Record{gender=%s, animated=%s, indeclinable=%s, plural='%s', singularCases=%s, pluralCases=%s}",
                    gender(), animate(), indeclinable(), plural, Arrays.toString(singularCases), Arrays.toString(pluralCases));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WordRecord record = (WordRecord) o;
            return characteristics == record.characteristics &&
                    Objects.equals(plural, record.plural) &&
                    Arrays.equals(singularCases, record.singularCases) &&
                    Arrays.equals(pluralCases, record.pluralCases);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(characteristics, plural);
            result = 31 * result + Arrays.hashCode(singularCases);
            result = 31 * result + Arrays.hashCode(pluralCases);
            return result;
        }
    }
}

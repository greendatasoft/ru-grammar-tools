package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.Case;
import com.gitlab.sszuev.inflector.Gender;

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

    private final Supplier<Map<String, WordRecord>> loader;
    // it is okay to have Map in memory: it is not so big (~20_000records),
    // but just in case store it as SoftReference:
    private volatile SoftReference<Map<String, WordRecord>> content;

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
     * Returns a word-info object.
     *
     * @param word {@code String}, not {@code null}
     * @return an {@code Optional} of {@link Word}
     */
    public Optional<Word> wordInfo(String word) {
        String key = MiscStringUtils.normalize(word, LOCALE);
        WordRecord record = contentMap().get(key);
        if (record == null) {
            return Optional.empty();
        }
        if (record instanceof SingleWordRecord) {
            return Optional.of(((SingleWordRecord) record));
        }
        return Optional.empty();
    }


    /**
     * Tries to find the correct form of the word from the dictionary according the given parameters.
     *
     * @param word       {@code String}, not {@code null}
     * @param declension {@link Case}, not {@code null}
     * @param gender     {@link  Gender}, can be {@code null}
     * @param animated   {@code Boolean}, can be {@code null}
     * @param plural     {@code Boolean}, can be {@code null}
     * @return {@code String} or {@code null}
     */
    public String inflect(String word, Case declension, Gender gender, Boolean animated, Boolean plural) {
        String key = MiscStringUtils.normalize(word, LOCALE);
        WordRecord record = contentMap().get(key);
        if (record == null) {
            return null;
        }
        SingleWordRecord single = selectSingleRecord(record, gender, animated);
        if (single.indeclinable != null && single.indeclinable) {
            return word;
        }
        if (declension == Case.NOMINATIVE) {
            return plural == Boolean.TRUE ? single.plural : word;
        }
        String[] cases = plural == Boolean.TRUE && single.pluralCases != null ? single.pluralCases : single.singularCases;
        if (cases == null) {
            return null;
        }
        String w = cases[declension.ordinal() - 1];
        return selectLongestWord(w);
    }

    protected SingleWordRecord selectSingleRecord(WordRecord record, Gender gender, Boolean animated) {
        if (record instanceof SingleWordRecord) {
            return (SingleWordRecord) record;
        }
        MultiWordRecord multi = (MultiWordRecord) record;
        List<SingleWordRecord> res = Arrays.stream(multi.words)
                .filter(s -> (gender == null || s.gender == gender) &&
                        (animated == null || s.animated == animated))
                .collect(Collectors.toList());
        if (res.isEmpty()) { // can't select, choose first
            return multi.words[0];
        }
        return res.get(0);
    }

    protected String selectLongestWord(String w) {
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

    protected Map<String, WordRecord> contentMap() {
        SoftReference<Map<String, WordRecord>> content = this.content;
        Map<String, WordRecord> res;
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
     * Loads the {@link Dictionary} from the file system.
     *
     * @param source {@code String} - resource
     * @return immutable {@code Map}
     */
    @SuppressWarnings({"unchecked"})
    protected static Map<String, WordRecord> load(String source) {
        Map<String, WordRecord> data = new HashMap<>(26900);
        try (InputStream in = Objects.requireNonNull(Dictionary.class.getResourceAsStream(source));
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Stream<String> lines = reader.lines()) {
            lines.forEach(record -> {
                Map.Entry<String, WordRecord> e = SingleWordRecord.parse(record);
                if (e == null) {
                    return;
                }
                data.merge(e.getKey(), e.getValue(), MultiWordRecord::create);
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Can't load " + source, e);
        }
        // MapN must be faster:
        Map.Entry<String, WordRecord>[] array = data.entrySet().toArray(Map.Entry[]::new);
        return Map.ofEntries(array);
    }

    interface WordRecord {
    }

    public interface Word {
        Gender gender();

        Boolean animate();
    }

    static class MultiWordRecord implements WordRecord {
        private final SingleWordRecord[] words;

        MultiWordRecord(SingleWordRecord[] words) {
            this.words = words;
        }

        static WordRecord create(WordRecord... records) {
            List<SingleWordRecord> res = new ArrayList<>();
            for (WordRecord w : records) {
                if (w instanceof SingleWordRecord) {
                    res.add((SingleWordRecord) w);
                } else {
                    res.addAll(Arrays.asList(((MultiWordRecord) w).words));
                }
            }
            return new MultiWordRecord(res.toArray(SingleWordRecord[]::new));
        }

        @Override
        public String toString() {
            return String.format("{%s}", Arrays.toString(words));
        }
    }

    static class SingleWordRecord implements WordRecord, Word {
        private Gender gender;
        private Boolean animated;
        private Boolean indeclinable;
        private String plural;
        private String[] singularCases;
        private String[] pluralCases;

        /**
         * Parses the csv-line.
         * The header:
         * {@code bare,accented,translations_en,translations_de,gender,partner,animate,indeclinable,sg_only,pl_only,
         * sg_nom,sg_gen,sg_dat,sg_acc,sg_inst,sg_prep,pl_nom,pl_gen,pl_dat,pl_acc,pl_inst,pl_prep}
         *
         * @param sourceLine {@code String}
         * @return a {@code Map.Entry}
         */
        private static Map.Entry<String, WordRecord> parse(String sourceLine) {
            String[] array = sourceLine.split("\t");
            String key = MiscStringUtils.normalize(Objects.requireNonNull(array[0]), LOCALE);
            if (array.length < 5) {
                return null;
            }
            SingleWordRecord res = new SingleWordRecord();
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
        public String toString() {
            return String.format("Record{gender=%s, animated=%s, indeclinable=%s, plural='%s', singularCases=%s, pluralCases=%s}",
                    gender, animated, indeclinable, plural, Arrays.toString(singularCases), Arrays.toString(pluralCases));
        }

        @Override
        public Gender gender() {
            return gender;
        }

        @Override
        public Boolean animate() {
            return animated;
        }
    }
}

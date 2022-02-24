package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.Case;
import com.gitlab.sszuev.inflector.Gender;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * A dictionary, contains words from resources.
 * Created by @ssz on 24.02.2022.
 *
 * @see <a href='https://github.com/Badestrand/russian-dictionary'>Russian Dictionary Data</a>
 */
public class Dictionary {
    public static final Locale LOCALE = new Locale("ru", "ru");

    private final Path source;
    // it is okay to have Map in memory: it is not so big (~20_000records),
    // but just in case store it as SoftReference:
    private volatile SoftReference<Map<String, WordInfo>> content;

    protected Dictionary(Path path) {
        this.source = Objects.requireNonNull(path);
    }

    /**
     * Singleton.
     *
     * @return {@link Dictionary}
     */
    public static Dictionary getInstance() {
        return NounDictionaryLoader.DICTIONARY;
    }

    /**
     * Tries to find the correct form of the word from the dictionary.
     *
     * @param word       {@code String}
     * @param declension {@link Case}
     * @param plural     {@code Boolean}
     * @return {@code String} or {@code null}
     */
    public String inflect(String word, Case declension, Boolean plural) {
        WordInfo res = contentMap().get(word);
        if (res == null) {
            return null;
        }
        if (res.indeclinable != null && res.indeclinable) {
            return word;
        }
        if (declension == Case.NOMINATIVE) {
            return plural == Boolean.TRUE ? res.plural : word;
        }
        String[] cases = plural == Boolean.TRUE && res.pluralCases != null ? res.pluralCases : res.singularCases;
        if (cases == null) {
            return null;
        }
        String w = cases[declension.ordinal() - 1];
        if (declension == Case.INSTRUMENTAL) {
            // can have two forms, returns the first one
            return w.split(",\\s*")[0];
        }
        return w;
    }

    protected Map<String, WordInfo> contentMap() {
        SoftReference<Map<String, WordInfo>> content = this.content;
        Map<String, WordInfo> res;
        if (content != null && (res = content.get()) != null) {
            return res;
        }
        synchronized (this) {
            content = this.content;
            if (content != null && (res = content.get()) != null) {
                return res;
            }
            this.content = new SoftReference<>(res = loadSource());
            return res;
        }
    }

    protected Map<String, WordInfo> loadSource() {
        return WordInfo.load(source);
    }

    static class NounDictionaryLoader {
        private static final Dictionary DICTIONARY = load();

        private static Dictionary load() {
            try {
                Path src = Paths.get(Objects.requireNonNull(Dictionary.class.getResource("/nouns.csv")).toURI());
                return new Dictionary(src);
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Can't load nouns dictionary");
            }
        }
    }

    static class WordInfo {
        private Gender gender;
        private Boolean animated;
        private Boolean indeclinable;
        private String plural;
        private String[] singularCases;
        private String[] pluralCases;

        /**
         * Loads the {@link Dictionary}.
         *
         * @param source {@link Path}
         * @return immutable {@code Map}
         */
        @SuppressWarnings({"unchecked"})
        public static Map<String, WordInfo> load(Path source) {
            Map<String, WordInfo> data = new HashMap<>(26900);
            try (Stream<String> s = Files.lines(source)) {
                s.forEach(x -> {
                    Map.Entry<String, WordInfo> e = parse(x);
                    if (e == null) {
                        return;
                    }
                    data.put(e.getKey(), e.getValue());
                });
            } catch (IOException e) {
                throw new UncheckedIOException("Can't load " + source, e);
            }
            // MapN must be faster:
            Map.Entry<String, WordInfo>[] array = data.entrySet().toArray(Map.Entry[]::new);
            return Map.ofEntries(array);
        }

        /**
         * Parses the csv-line.
         * The header:
         * {@code bare,accented,translations_en,translations_de,gender,partner,animate,indeclinable,sg_only,pl_only,
         * sg_nom,sg_gen,sg_dat,sg_acc,sg_inst,sg_prep,pl_nom,pl_gen,pl_dat,pl_acc,pl_inst,pl_prep}
         *
         * @param sourceLine {@code String}
         * @return a {@code Map.Entry}
         */
        private static Map.Entry<String, WordInfo> parse(String sourceLine) {
            String[] array = sourceLine.split("\t");
            String key = normalizeKey(Objects.requireNonNull(array[0]));
            if (array.length < 5) {
                return null;
            }
            WordInfo res = new WordInfo();
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
                return Gender.NEUTER;
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

        private static String normalizeKey(String key) {
            return key.trim().toLowerCase(LOCALE);
        }

        private static String normalizeValue(String value) {
            return value.trim().toLowerCase(LOCALE).replace("'", "");
        }

        @Override
        public String toString() {
            return "WordInfo{" +
                    "gender=" + gender +
                    ", animated=" + animated +
                    ", indeclinable=" + indeclinable +
                    ", plural='" + plural + '\'' +
                    ", singularCases=" + Arrays.toString(singularCases) +
                    ", pluralCases=" + Arrays.toString(pluralCases) +
                    '}';
        }
    }
}

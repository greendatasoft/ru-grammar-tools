package pro.greendata.rugrammartools.impl.dictionaries;

import pro.greendata.rugrammartools.impl.utils.RuleUtils;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A dictionary, it contains words from resources (csv-file).
 * Created by @ssz on 24.02.2022.
 *
 * @see <a href='https://github.com/Badestrand/russian-dictionary'>Russian Dictionary Data</a>
 */
public abstract class Dictionary {

    private final Supplier<Map<String, Record>> loader;
    // it is okay to have Map in memory: it is not so big (~20_000 records (file size = 8MB),
    // but just in case store it as SoftReference:
    private volatile SoftReference<Map<String, Record>> content;

    protected Dictionary(String path, Function<String, Map.Entry<String, ? extends Record>> parser) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(parser);
        this.loader = () -> load(path, 26900, parser);
    }

    /**
     * Returns a dictionary, that contains nouns.
     *
     * @return {@link NounDictionary}
     */
    public static NounDictionary getNounDictionary() {
        return NounDictionary.DICTIONARY;
    }

    public static AdjectiveDictionary getAdjectiveDictionary() {
        return AdjectiveDictionary.DICTIONARY;
    }

    /**
     * Loads the {@link Dictionary} from the file system.
     *
     * @param source   {@code String} - resource
     * @param capacity {@code int} - approximate number of records
     * @param parser   {@code Function} - a method to parse line
     * @return immutable {@code Map}
     */
    @SuppressWarnings({"unchecked"})
    protected static Map<String, Record> load(String source,
                                              int capacity,
                                              Function<String, Map.Entry<String, ? extends Record>> parser) {
        Map<String, Record> data = new HashMap<>(capacity);
        Map<Record, Record> cache = new HashMap<>(capacity);
        try (InputStream in = Objects.requireNonNull(Dictionary.class.getResourceAsStream(source));
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Stream<String> lines = reader.lines()) {
            lines.forEach(record -> {
                Map.Entry<String, ? extends Record> e = parser.apply(record);
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
     * @return an {@code Optional} of {@link Record}
     */
    public abstract Optional<? extends Dictionary.Record> wordDetails(String word);

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
    public interface Record {
    }

    /**
     * Implementation that holds several {@link Record}s.
     */
    protected static class MultiRecord implements Record {
        protected final Record[] words;

        MultiRecord(Record[] words) {
            this.words = words;
        }

        public static Record create(Record... records) {
            List<Record> res = new ArrayList<>();
            for (Record w : records) {
                if (w instanceof MultiRecord) {
                    res.addAll(Arrays.asList(((MultiRecord) w).words));
                } else {
                    res.add(w);
                }
            }
            return new MultiRecord(res.toArray(Record[]::new));
        }

        @Override
        public String toString() {
            return String.format("{%s}", Arrays.toString(words));
        }
    }

    protected static abstract class BaseRecordImpl implements Record {
        public static final Comparator<BaseRecordImpl> FULLNESS_COMPARATOR =
                Comparator.comparingInt(BaseRecordImpl::fullness).reversed();

        protected int characteristics;

        public static String normalizeValue(String value) {
            return TextUtils.normalize(value).replace("'", "");
        }

        public static String toEnding(String key, String word) {
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

        /**
         * Answers a degree of fullness.
         *
         * @return {@code int}
         */
        protected abstract int fullness();

        protected Boolean hasCharacteristics(int has, int is) {
            return hasCharacteristics(has) ? hasCharacteristics(is) : null;
        }

        protected boolean hasCharacteristics(int ch) {
            return (characteristics & ch) == ch;
        }

        protected void setCharacteristics(Boolean flag, int has, int is) {
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

        protected void characteristicsOn(int ch) {
            characteristics = characteristics | ch;
        }

        protected void characteristicsOff(int ch) {
            if (!hasCharacteristics(ch)) {
                return;
            }
            characteristics = characteristics ^ ch;
        }
    }
}

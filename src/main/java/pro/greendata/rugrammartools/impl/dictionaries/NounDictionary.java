package pro.greendata.rugrammartools.impl.dictionaries;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.PartOfSpeech;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A dictionary, it contains nouns from the resource {@code nouns.csv} file.
 * Created by @ssz on 24.02.2022.
 *
 * @see <a href='https://github.com/Badestrand/russian-dictionary'>Russian Dictionary Data</a>
 */
public class NounDictionary extends Dictionary {

    static final NounDictionary DICTIONARY = new NounDictionary("/nouns.csv");

    protected NounDictionary(String path) {
        super(path, Word::parse);
    }

    /**
     * Returns a word-info object.
     *
     * @param word {@code String}, not {@code null}
     * @return an {@code Optional} of {@link Record}
     */
    @Override
    public Optional<Word> wordDetails(String word) {
        return wordDetails(word, null, null, null);
    }

    /**
     * Tries to find the most suitable word record object.
     *
     * @param key     {@code String}, the key (normalized: lowercase without trailing spaces), not {@code null}
     * @param gender  {@link Gender} a filter parameter, can be {@code null}
     * @param animate {@code Boolean}a filter parameter, can be {@code null}
     * @return an {@code Optional} of {@link Record}
     */
    public Optional<Word> wordDetails(String key, Gender gender, Boolean animate, Boolean isPlural) {
        Record record = contentMap().get(key);
        if (record == null) {
            return Optional.empty();
        }
        Word word = selectSingleRecord(record, gender, animate, isPlural);
        if (word == null) {
            return Optional.empty();
        }
        return Optional.of(word);
    }

    /**
     * Tries best to select the most appropriate word record according to the specified parameters.
     *
     * @param record   {@link Record}
     * @param gender   {@code Gender}
     * @param animated {@code Boolean}
     * @return {@link Word}
     */
    protected Word selectSingleRecord(Record record, Gender gender, Boolean animated, Boolean isPlural) {
        if (record instanceof Word) {
            Word word = (Word) record;
            if (isPlural == null) {
                return word;
            }

            if (word.isPluralKey == isPlural) {
                return word;
            }
            return null;
        }
        MultiRecord multi = (MultiRecord) record;
        List<Word> res = Arrays.stream(multi.words)
                .map(r -> (Word) r)
                .sorted(BaseRecordImpl.FULLNESS_COMPARATOR)
                .filter(w -> (gender == null || w.gender() == gender) && (animated == null || w.animate() == animated) && (isPlural == null || w.isPluralKey() == isPlural))
                .collect(Collectors.toList());
        if (res.isEmpty()) { // can't select, choose first
            return (Word) multi.words[0];
        }
        return res.get(0);
    }

    /**
     * A record for noun (right now).
     */
    public static class Word extends BaseRecordImpl implements Record {
        private static final int HAS_ANIMATE = 2;
        private static final int IS_ANIMATE = 4;
        private static final int HAS_INDECLINABLE = 8;
        private static final int IS_INDECLINABLE = 16;
        private static final int GENDER_FLAG_0 = 32;
        private static final int GENDER_FLAG_1 = 64;

        private String singular;
        private String plural;
        private String[] singularCases;
        private String[] pluralCases;
        private boolean isPluralKey;

        /**
         * Parses the csv-line (for noun).
         * The header:
         * {@code bare,accented,translations_en,translations_de,gender,partner,animate,indeclinable,sg_only,pl_only,
         * sg_nom,sg_gen,sg_dat,sg_acc,sg_inst,sg_prep,pl_nom,pl_gen,pl_dat,pl_acc,pl_inst,pl_prep}
         *
         * @param sourceLine {@code String}
         * @return a {@code Map.Entry}
         */
        private static Map<String, Word> parse(String sourceLine) {
            String[] array = sourceLine.split("\t");
            String key = TextUtils.normalize(Objects.requireNonNull(array[0]));
            Word singularWord = toParse(key, array);

            if (singularWord == null) {
                return null;
            }
            singularWord.isPluralKey = false;

            if (array.length < 22) {
                return Map.of(key, singularWord);
            }

            String pluralKey = normalizeKey(array[16]);
            if (key.equals(pluralKey)) {
                singularWord.isPluralKey = true;
                return Map.of(key, singularWord);
            }

            Word pluralWord = toParse(pluralKey, array);
            if (pluralWord != null) {
                pluralWord.isPluralKey = true;
                return Map.of(key, singularWord, pluralKey, pluralWord);
            }
            return Map.of(key, singularWord);
        }

        private static Word toParse(String key, String[] array) {
            if (array.length < 5) {
                return null;
            }
            Word res = new Word();
            res.gender(parseGender(array[4]));
            if (array.length < 7) {
                return res;
            }
            res.animate(parseBoolean(array[6]));
            if (array.length < 8) {
                return res;
            }
            res.indeclinable(parseBoolean(array[7]));
            if (res.indeclinable() == Boolean.TRUE) {
                return res;
            }
            if (array.length < 16) {
                return res;
            }
            res.singular = toEnding(key, array[10]);
            res.singularCases = new String[5];
            for (int i = 0; i < 5; i++) {
                res.singularCases[i] = toEnding(key, array[11 + i]);
            }
            if (array.length < 22) {
                return res;
            }
            res.plural = toEnding(key, array[16]);
            res.pluralCases = new String[5];
            for (int i = 0; i < 5; i++) { // note that the base here is a singular key, not plural its form
                res.pluralCases[i] = toEnding(key, array[17 + i]);
            }

            return res;
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

        public Boolean animate() {
            return hasCharacteristics(HAS_ANIMATE, IS_ANIMATE);
        }

        public final PartOfSpeech partOfSpeech() {
            return PartOfSpeech.NOUN;
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

        public String singular() {
            return singular;
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

        public boolean isIndeclinable() {
            return indeclinable() != null && indeclinable();
        }

        public boolean isPluralKey() {
            return isPluralKey;
        }

        @Override
        protected int fullness() {
            return Stream.of(gender(), animate(), indeclinable(), plural, isPluralKey, singularCases, pluralCases, singular)
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
            Word record = (Word) o;
            return characteristics == record.characteristics &&
                    Objects.equals(plural, record.plural) &&
                    Objects.equals(singular, record.singular) &&
                    Objects.equals(isPluralKey, record.isPluralKey) &&
                    Arrays.equals(singularCases, record.singularCases) &&
                    Arrays.equals(pluralCases, record.pluralCases);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(characteristics, plural, isPluralKey, singular);
            result = 31 * result + Arrays.hashCode(singularCases);
            result = 31 * result + Arrays.hashCode(pluralCases);
            return result;
        }
    }
}

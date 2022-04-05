package pro.greendata.rugrammartools.impl.dictionaries;


import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.PartOfSpeech;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A dictionary, it contains adjectives from the resource {@code adjectives.csv} file.
 * Created by @loginov on 05.04.2022.
 *
 * @see <a href='https://github.com/Badestrand/russian-dictionary'>Russian Dictionary Data</a>
 */
public class AdjectiveDictionary extends Dictionary {
    static final AdjectiveDictionary DICTIONARY = new AdjectiveDictionary("/adjectives.csv");

    protected AdjectiveDictionary(String path) {
        super(path, Word::parse);
    }

    @Override
    public Optional<Word> wordDetails(String word) {
        Record record = contentMap().get(word);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(selectSingleRecord(record));
    }

    //TODO
    protected Word selectSingleRecord(Record record) {
        if (record instanceof Word) {
            return (Word) record;
        }
        MultiRecord multi = (MultiRecord) record;
        List<Word> res = Arrays.stream(multi.words)
                .map(r -> (Word) r)
                .sorted(BaseRecordImpl.FULLNESS_COMPARATOR)
                .collect(Collectors.toList());
        if (res.isEmpty()) { // can't select, choose first
            return (Word) multi.words[0];
        }
        return res.get(0);
    }

    public static class Word extends BaseRecordImpl implements Record {
        private String[] masculineCases;
        private String[] feminineCases;
        private String[] neuterCases;
        private String[] pluralCases;

        /**
         * Parse the csv-line (for a).
         * The header:
         * {@code bare, accented, translations_en, translations_de, comparative, superlative,
         * short_m, short_f, short_n, short_pl,
         * decl_m_nom, decl_m_gen, decl_m_dat, decl_m_acc, decl_m_inst, decl_m_prep,
         * decl_f_nom, decl_f_gen, decl_f_dat, decl_f_acc, decl_f_inst, decl_f_prep,
         * decl_n_nom, decl_n_gen, decl_n_dat, decl_n_acc, decl_n_inst, decl_n_prep,
         * decl_pl_nom,	decl_pl_gen, decl_pl_dat, decl_pl_acc, decl_pl_inst, decl_pl_prep}
         *
         * @param sourceLine {@code String}
         * @return a {@code Map.Entry}
         */
        private static Map.Entry<String, Word> parse(String sourceLine) {
            String[] array = sourceLine.split("\t");
            String key = TextUtils.normalize(Objects.requireNonNull(array[0]));
            if (array.length < 10) {
                return null;
            }
            Word res = new Word();
            res.masculineCases = new String[6];
            if (array.length < 16) {
                return Map.entry(key, res);
            }
            for (int i = 0; i < 6; i++) {
                res.masculineCases[i] = toEnding(key, array[10 + i]);
            }
            if (array.length < 22) {
                return Map.entry(key, res);
            }
            res.feminineCases = new String[6];
            for (int i = 0; i < 6; i++) {
                res.feminineCases[i] = toEnding(key, array[16 + i]);
            }
            if (array.length < 28) {
                return Map.entry(key, res);
            }
            res.neuterCases = new String[6];
            for (int i = 0; i < 6; i++) {
                res.neuterCases[i] = toEnding(key, array[22 + i]);
            }
            if (array.length < 34) {
                return Map.entry(key, res);
            }
            res.pluralCases = new String[6];
            for (int i = 0; i < 6; i++) { // note that the base here is a singular key, not plural its form
                res.pluralCases[i] = toEnding(key, array[28 + i]);
            }
            return Map.entry(key, res);
        }

        public final PartOfSpeech partOfSpeech() {
            return PartOfSpeech.ADJECTIVE;
        }

        public String[] masculineCases() {
            return masculineCases;
        }

        public String[] feminineCases() {
            return feminineCases;
        }

        public String[] neuterCases() {
            return neuterCases;
        }

        public String[] pluralCases() {
            return pluralCases;
        }

        @Override
        protected int fullness() {
            return Stream.of(masculineCases, feminineCases, neuterCases, pluralCases)
                    .filter(Objects::nonNull).mapToInt(x -> 1).sum();
        }

        @Override
        public String toString() {
            return String.format("Record{masculineCases=%s, feminineCases=%s, neuterCases=%s, pluralCases=%s}",
                    Arrays.toString(masculineCases), Arrays.toString(feminineCases), Arrays.toString(neuterCases),
                    Arrays.toString(pluralCases));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Word record = (Word) o;
            return characteristics == record.characteristics &&
                    Arrays.equals(masculineCases, record.masculineCases) &&
                    Arrays.equals(feminineCases, record.feminineCases) &&
                    Arrays.equals(neuterCases, record.neuterCases) &&
                    Arrays.equals(pluralCases, record.pluralCases);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(characteristics);
            result = 31 * result + Arrays.hashCode(masculineCases);
            result = 31 * result + Arrays.hashCode(feminineCases);
            result = 31 * result + Arrays.hashCode(neuterCases);
            result = 31 * result + Arrays.hashCode(pluralCases);
            return result;
        }
    }
}

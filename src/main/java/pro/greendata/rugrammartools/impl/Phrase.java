package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.utils.GrammarUtils;
import pro.greendata.rugrammartools.impl.utils.MiscStringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a phrase.
 */
public class Phrase {
    private final String raw;
    private final List<String> keys;
    private final List<String> words;
    private final List<Word> details;
    private final List<String> separators;
    private final Gender gender;
    private final Boolean animate;

    protected Phrase(String phrase,
                     Gender gender,
                     Boolean animate,
                     List<String> keys,
                     List<String> words,
                     List<Word> details,
                     List<String> separators) {
        this.raw = phrase;
        this.gender = gender;
        this.animate = animate;
        this.keys = Objects.requireNonNull(keys);
        this.words = Objects.requireNonNull(words);
        this.details = Objects.requireNonNull(details);
        this.separators = Objects.requireNonNull(separators);
    }

    /**
     * Creates a phrase object from the given string.
     *
     * @param phrase  {@code String}, not {@code null}
     * @param gender  {@link Gender} (can be {@code null}), filter parameter
     * @param animate {@code Boolean} (can be {@code null}), filter parameter
     * @return {@link Phrase}
     */
    public static Phrase parse(String phrase, Gender gender, Boolean animate) {
        String[] parts = split(phrase);
        if (parts.length == 1) {
            return parseWord(parts[0], gender, animate);
        }
        return parseWords(phrase, parts, gender, animate);
    }

    private static Phrase parseWord(String word, Gender gender, Boolean animate) {
        String key = toKey(word);
        List<String> keys = new ArrayList<>();
        List<String> words = new ArrayList<>();
        List<Word> details = new ArrayList<>();
        List<String> separators = new ArrayList<>();
        Optional<Word> fromDictionary = findWordDetails(key, gender, animate);
        if (fromDictionary.isPresent()) {
            Word detail = createWordDetails(key, fromDictionary.get(), gender, animate);
            keys.add(key);
            words.add(word);
            details.add(detail);
            return create(word, detail.gender(), detail.animate(), keys, words, details, separators);
        }
        gender = gender == null ? GrammarUtils.guessGenderOfSingularNoun(key) : gender;
        if (!word.contains("-")) {
            fill(word, new WordDetailImpl(gender, animate, false, null), keys, words, details);
        } else {
            fillWithHyphen(word, gender, animate, keys, words, details, separators);
        }
        return create(word, gender, animate, keys, words, details, separators);
    }

    private static Phrase parseWords(String raw, String[] parts, Gender gender, Boolean animate) {
        // usually gender is null, it is determined by the phrase; may not match the true gender of the wearer (in case of profession).
        int noun = 0; // the position of the main word in the phrase
        for (int i = 0; i < parts.length; i++) {
            String w = parts[i];
            // preposition (e.g. "Термист по обработке слюды")
            if (i != 0 && GrammarUtils.canBeNonDerivativePreposition(w)) {
                break;
            }
            // (masculine) skip leading adjectives
            if ((gender == null || gender == Gender.MALE) && GrammarUtils.canBeSingularNominativeMasculineAdjective(w)) {
                gender = Gender.MALE;
                if (GrammarUtils.canBeMasculineAdjectiveBasedSubstantivatNoun(w)) {
                    noun = i;
                    break;
                }
                continue;
            }
            // (feminine) skip leading adjectives
            if ((gender == null || gender == Gender.FEMALE) && GrammarUtils.canBeSingularNominativeFeminineAdjective(w)) {
                gender = Gender.FEMALE;
                if (GrammarUtils.canBeFeminineAdjectiveBasedSubstantivatNoun(w)) {
                    noun = i;
                    break;
                }
                continue;
            }
            // (neuter) skip leading adjectives
            if ((gender == null || gender == Gender.NEUTER) && GrammarUtils.canBeSingularNominativeNeuterAdjective(w)) {
                gender = Gender.NEUTER;
                continue;
            }
            noun = i;
            break;
        }
        Word nounDetails = fetchWordDetails(parts[noun], gender, animate);
        int end = findEndIndex(parts, noun, nounDetails.gender());
        return assemble(raw, parts, end, nounDetails, noun);
    }

    private static Word fetchWordDetails(String word, Gender gender, Boolean animate) {
        String key = toKey(word);
        Optional<Word> res = findWordDetails(key, gender, animate);
        return createWordDetails(key, res.orElse(null), gender, animate);
    }

    private static Word createWordDetails(String key, Word from, Gender gender, Boolean animate) {
        if (gender == null) {
            if (from != null) {
                gender = from.gender();
            }
            if (gender == null) {
                gender = GrammarUtils.guessGenderOfSingularNoun(key);
            }
        }
        if (animate == null) {
            animate = from != null ? from.animate() : null;
        }
        boolean indeclinable = from != null && from.isIndeclinable();
        return new WordDetailImpl(gender, animate, indeclinable, from);
    }

    private static Optional<Word> findWordDetails(String key, Gender gender, Boolean animate) {
        return Dictionary.getNounDictionary().wordDetails(key, gender, animate);
    }

    private static int findEndIndex(String[] parts, int noun, Gender gender) {
        // only the first part of the phrase is declined - the main word (noun) and the adjectives surrounding it;
        // the words after - usually does not decline (we assume that some supplemental part goes next)
        int end = noun;
        for (int i = noun + 1; i < parts.length; i++) {
            if (!GrammarUtils.canBeAdjective(parts[i], gender)) {
                break;
            }
            end = i;
        }
        if (end != noun && end != parts.length - 1) {
            // TODO: phrases with two nouns are ignored for now
            end = noun;
        }
        return end;
    }

    private static Phrase assemble(String raw, String[] parts, int endIndex, Word noun, int nounIndex) {
        List<String> keys = new ArrayList<>();
        List<String> words = new ArrayList<>();
        List<Word> details = new ArrayList<>();
        List<String> separators = new ArrayList<>();
        if (endIndex == 0) {
            String w = parts[0];
            if (w.contains("-")) {
                fillWithHyphen(w, noun.gender(), noun.animate(), keys, words, details, separators);
            } else {
                fill(w, noun, keys, words, details);
            }
        } else {
            for (int i = 0; i <= endIndex; i++) {
                String w = parts[i];
                if (w.contains("-")) {
                    fillWithHyphen(w, noun.gender(), noun.animate(), keys, words, details, separators);
                } else {
                    Word d = i == nounIndex ? noun : new WordDetailImpl(noun.gender(), noun.animate(), false, null);
                    fill(w, d, keys, words, details);
                }
                if (i != endIndex) {
                    separators.add(" ");
                }
            }
        }
        // the rest part of phrase is indeclinable
        for (int i = endIndex + 1; i < parts.length; i++) {
            separators.add(" ");
            String k = toKey(parts[i]);
            keys.add(k);
            words.add(parts[i]);
            details.add(new WordDetailImpl(null, null, true, null));
        }
        return create(raw, noun.gender(), noun.animate(), keys, words, details, separators);
    }

    /**
     * Creates a phrase instance.
     *
     * @param raw        a {@code String} original phrase, not {@code null}
     * @param gender     a {@link Gender} of whole phrase, can be {@code null}
     * @param animate    a {@code Boolean} parameter of whole phrase,
     *                   {@code true} for animate, {@code false} for inanimate, {@code null} for unspecified
     * @param keys       a {@link List} of normalized phrase parts,
     *                   i.e. words in lowercase without trailing spaces, not {@code null}
     * @param words      a {@link List} of origin phrase parts, i.e. words, not {@code null}
     * @param details    a {@link List} of phrase parts details, not {@code null}
     * @param separators a {@link List} of separators between phrase parts, not {@code null}
     * @return {@link Phrase}
     */
    protected static Phrase create(String raw,
                                   Gender gender, Boolean animate,
                                   List<String> keys, List<String> words, List<Word> details, List<String> separators) {
        Objects.requireNonNull(raw, "null phrase");
        if (separators.size() != details.size() - 1) {
            throw new IllegalArgumentException();
        }
        if (details.size() != words.size()) {
            throw new IllegalArgumentException();
        }
        if (keys.size() != words.size()) {
            throw new IllegalArgumentException();
        }
        return new Phrase(raw, gender, animate,
                Collections.unmodifiableList(keys), Collections.unmodifiableList(words),
                Collections.unmodifiableList(details), Collections.unmodifiableList(separators));
    }

    private static void fillWithHyphen(String word, Gender gender, Boolean animated,
                                       List<String> keys, List<String> words, List<Word> details, List<String> separators) {
        int prev = 0;
        Matcher m = Pattern.compile("-").matcher(word);
        Gender g = gender;
        while (m.find()) {
            String w = word.substring(prev, m.start());
            String key = toKey(w);
            keys.add(key);
            words.add(w);
            // todo: use dictionary here
            details.add(new WordDetailImpl(g, animated, false, null));
            separators.add(m.group());
            prev = m.end();
            // the second part is usually in the masculine gender (e.g. "сестра-анестезист")
            g = Gender.MALE;
        }
        String w = word.substring(prev);
        String key = toKey(w);
        keys.add(key);
        words.add(w);
        // todo: use dictionary here
        details.add(new WordDetailImpl(g, animated, false, null));
    }

    private static void fill(String word, Word info, List<String> keys, List<String> words, List<Word> details) {
        String key = toKey(word);
        keys.add(key);
        words.add(word);
        details.add(info);
    }

    private static String toKey(String w) {
        return MiscStringUtils.normalize(w, Dictionary.LOCALE);
    }

    /**
     * Splits the phrase on parts (i.e. words) using separators.
     * Note: the final {@link Phrase} may have other separators.
     *
     * @param phrase {@code String}
     * @return an {@code Array} of {@code String}s
     */
    public static String[] split(String phrase) {
        String[] res = phrase.trim().split("\\s+");
        if (res.length == 0) {
            throw new IllegalArgumentException();
        }
        return res;
    }

    /**
     * Glues a phrase parts back into single {@code String}-phrase.
     *
     * @param parts      a {@link List} of {@code String}s - words
     * @param separators a {@link List} of {@code String}s
     * @return a {@code String}
     */
    public static String compose(List<String> parts, List<String> separators) {
        if (separators.size() != parts.size() - 1) {
            throw new IllegalArgumentException();
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            res.append(parts.get(i));
            if (i != parts.size() - 1) {
                res.append(separators.get(i));
            }
        }
        return res.toString();
    }

    public Gender gender() {
        return gender;
    }

    public Boolean animate() {
        return animate;
    }

    public String raw() {
        return raw;
    }

    public int length() {
        return keys.size();
    }

    public String key(int i) {
        return keys.get(i);
    }

    public String original(int i) {
        return words.get(i);
    }

    public Word details(int i) {
        return details.get(i);
    }

    public List<String> separators() {
        return separators;
    }

    @Override
    public String toString() {
        return String.format("Phrase{raw='%s', words=%s}", raw, keys);
    }

    public static class WordDetailImpl implements Word {
        private final Gender gender;
        private final Boolean animated;
        private final boolean indeclinable;
        private final Word from;

        public WordDetailImpl(Gender gender, Boolean animated, boolean indeclinable, Word from) {
            this.gender = gender;
            this.animated = animated;
            this.indeclinable = indeclinable;
            this.from = from;
        }

        @Override
        public Gender gender() {
            return gender;
        }

        @Override
        public Boolean animate() {
            return animated;
        }

        @Override
        public boolean isIndeclinable() {
            return indeclinable;
        }

        @Override
        public String[] singularCases() {
            return from != null ? from.singularCases() : null;
        }

        @Override
        public String plural() {
            return from != null ? from.plural() : null;
        }

        @Override
        public String[] pluralCases() {
            return from != null ? from.pluralCases() : null;
        }

        @Override
        public String toString() {
            return String.format("Details{gender=%s, animated=%s, indeclinable=%s, from=%s}",
                    gender, animated, indeclinable, from);
        }
    }
}

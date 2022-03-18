package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.utils.GrammarUtils;
import pro.greendata.rugrammartools.impl.utils.NameUtils;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.util.*;

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
    private final Boolean plural;

    protected Phrase(String phrase,
                     Gender gender,
                     Boolean animate,
                     Boolean plural,
                     List<String> keys,
                     List<String> words,
                     List<Word> details,
                     List<String> separators) {
        this.raw = phrase;
        this.gender = gender;
        this.animate = animate;
        this.plural = plural;
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
        Assembler res = Assembler.split(phrase);
        if (res.parts.isEmpty()) {
            throw new IllegalStateException();
        }
        res.compile(gender, animate);
        return res.toPhrase();
    }

    private static Optional<Word> fromDictionary(String key, Gender gender, Boolean animate) {
        return Dictionary.getNounDictionary().wordDetails(key, gender, animate);
    }

    private static String toKey(String w) {
        return TextUtils.normalize(w, Dictionary.LOCALE);
    }

    /**
     * Glues a phrase parts back into single {@code String}-phrase.
     *
     * @param parts      a {@link List} of {@code String}s - words
     * @param separators a {@link List} of {@code String}s
     * @return a {@code String}
     */
    public static String compose(List<String> parts, List<String> separators) {
        if (separators.size() != parts.size() + 1) {
            throw new IllegalArgumentException();
        }
        StringBuilder res = new StringBuilder();
        res.append(separators.get(0));
        for (int i = 0; i < parts.size(); i++) {
            res.append(parts.get(i));
            res.append(separators.get(i + 1));
        }
        return res.toString();
    }

    public Gender gender() {
        return gender;
    }

    public Boolean animate() {
        return animate;
    }

    public Boolean plural() {
        return plural;
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

    /**
     * Mutable object-builder.
     */
    private static class Assembler {
        private static final int INDEX_STEP = 1000;

        private final NavigableMap<Integer, Part> parts = new TreeMap<>();
        // input phrase
        private String raw;
        // the position of the main noun in the phrase
        private Integer nounStartIndex;
        // for composed nouns end != start (e.g. "сестра-анестезист")
        private Integer nounEndIndex;
        // end of declinable part of phrase
        private Integer endIndex;
        // usually gender is null, it is determined by the phrase;
        // may not match the true gender of the wearer (in case of profession).
        private Gender gender;
        // can be null
        private Boolean animate;
        private String leadingSpace;
        private String trailingSpace;

        /**
         * Splits the given {@code phrase} into parts wrapped as {@link Assembler} object.
         * If the phrase contains {@link #isStopSymbol(char) stop symbol},
         * then the rest part of phrase after that symbol inclusively is considered as indeclinable.
         *
         * @param phrase {@code String}, not {@code null}
         * @return {@link Assembler}
         */
        static Assembler split(String phrase) {
            Assembler res = new Assembler();
            res.raw = phrase;
            char[] chars = phrase.toCharArray();
            StringBuilder word = new StringBuilder();
            StringBuilder separator = new StringBuilder();
            int index = 0;
            for (int i = 0; i < chars.length; ) {
                // leading space
                while (i < chars.length && isSpace(chars[i])) {
                    separator.append(chars[i++]);
                }
                if (i == chars.length) {
                    break;
                }
                if (res.parts.isEmpty()) {
                    res.leadingSpace = separator.toString();
                    separator = new StringBuilder();
                }
                // process next word
                if (isStopSymbol(chars[i])) {
                    int j = chars.length - 1;
                    for (; j >= i; j--) {
                        if (!isSpace(chars[j])) {
                            break;
                        }
                    }
                    word.append(chars, i, j - i + 1);
                    Part p = new Part(word.toString());
                    p.indeclinable = true;
                    res.parts.put(index * INDEX_STEP, p);
                    separator.append(chars, j, chars.length - j - 1);
                    res.trailingSpace = separator.toString();
                    break;
                }
                while (i < chars.length && !isSpace(chars[i])) {
                    word.append(chars[i++]);
                }
                Part p = new Part(word.toString());
                word = new StringBuilder();
                res.parts.put(index++ * INDEX_STEP, p);
                if (i == chars.length) {
                    break;
                }
                // process next separator
                while (i < chars.length && isSpace(chars[i])) {
                    separator.append(chars[i++]);
                }
                if (i == chars.length) {
                    res.trailingSpace = separator.toString();
                    break;
                }
                p.space = separator.toString();
                separator = new StringBuilder();
            }
            return res;
        }

        private static boolean isSpace(char ch) {
            return Character.isWhitespace(ch);
        }

        private static boolean isStopSymbol(char ch) {
            return ch == '\'' || ch == '"' || ch == '\u00AB';
        }

        /**
         * Compiles this object collecting its content.
         *
         * @param inputGender  {@link Gender}, can be {@code null}
         * @param inputAnimate {@code Boolean}, can be {@code null}
         */
        void compile(Gender inputGender, Boolean inputAnimate) {
            fill(inputGender, inputAnimate);

            findAndSetNounPosition(this);
            compileNoun(this);
            findAndSetEndPosition(this);

            this.parts.headMap(this.endIndex, true).forEach((i, p) -> {
                if (p.isBlank()) {
                    p.fill(gender, animate, false);
                }
            });
            this.parts.tailMap(this.endIndex, false).forEach((i, p) -> p.fill(gender, animate, true));
        }

        /**
         * Builds an immutable phrase.
         *
         * @return {@link Phrase}
         */
        public Phrase toPhrase() {
            List<String> separators = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            List<String> words = new ArrayList<>();
            List<Word> details = new ArrayList<>();

            separators.add(leadingSpace == null ? "" : leadingSpace);
            Integer last = parts.lastKey();
            Part noun = Objects.requireNonNull(parts.get(nounStartIndex));
            parts.forEach((index, part) -> {
                if (!Objects.equals(index, last)) {
                    separators.add(Objects.requireNonNull(part.space));
                }
                words.add(part.raw);
                keys.add(part.key());
                details.add(part.toWord());
            });
            separators.add(trailingSpace == null ? "" : trailingSpace);

            return new Phrase(raw, noun.gender, noun.animate,
                    noun.plural, Collections.unmodifiableList(keys), Collections.unmodifiableList(words),
                    Collections.unmodifiableList(details), Collections.unmodifiableList(separators));
        }

        private boolean isNullOr(Gender g) {
            return gender == null || gender == g;
        }

        private void fill(Part p) {
            fill(p.gender, p.animate);
        }

        private void fill(Gender g, Boolean a) {
            if (this.animate == null) {
                this.animate = a;
            }
            if (this.gender == null) {
                this.gender = g;
            }
        }

        private static void findAndSetNounPosition(Assembler phrase) {
            phrase.nounStartIndex = phrase.parts.firstKey(); // the position of the main word in the phrase
            for (Integer index : phrase.parts.keySet()) {
                Part part = phrase.parts.get(index);
                String w = part.raw;
                Map.Entry<Integer, Part> next = phrase.parts.higherEntry(index);
                if (GrammarUtils.canBeAbbreviation(w, phrase.raw) || NameUtils.canBeInitials(w)) { // "ПАО 'Финансовая корпорация'"
                    part.fill(phrase.gender, phrase.animate, true);
                    if (next != null && NameUtils.canBeSurname(next.getValue().raw)) {
                        phrase.nounStartIndex = next.getKey(); // "И.П. Иванов", "ИП Иванов"
                    } else {
                        phrase.nounStartIndex = index;
                    }
                    break;
                }
                // if the next word is preposition then the first word can be noun (e.g. "Термист по обработке слюды")
                if (next != null && GrammarUtils.isNonDerivativePreposition(next.getValue().raw)) {
                    phrase.nounStartIndex = index;
                    break;
                }
                // (masculine) skip leading adjectives
                if (phrase.isNullOr(Gender.MALE) && GrammarUtils.canBeSingularNominativeMasculineAdjective(w)) {
                    phrase.gender = Gender.MALE;
                    if (GrammarUtils.canBeMasculineAdjectiveBasedSubstantivatNoun(w)) {
                        phrase.nounStartIndex = index;
                        break;
                    }
                    continue;
                }
                // (feminine) skip the leading adjectives
                if (phrase.isNullOr(Gender.FEMALE) && GrammarUtils.canBeSingularNominativeFeminineAdjective(w)) {
                    phrase.gender = Gender.FEMALE;
                    if (GrammarUtils.canBeFeminineAdjectiveBasedSubstantivatNoun(w)) {
                        phrase.nounStartIndex = index;
                        break;
                    }
                    continue;
                }
                // (neuter) skip leading adjectives
                if (phrase.isNullOr(Gender.NEUTER) && GrammarUtils.canBeSingularNominativeNeuterAdjective(w)) {
                    phrase.gender = Gender.NEUTER;
                    continue;
                }
                phrase.nounStartIndex = index;
                break;
            }
        }

        private static void findAndSetEndPosition(Assembler phrase) {
            // only the first part of the phrase is declined - the main word (noun) and the adjectives surrounding it;
            // the words after - usually does not decline (we assume that some supplemental part goes next)
            phrase.endIndex = phrase.nounEndIndex;
            Integer lastIndex = phrase.parts.lastKey();
            NavigableMap<Integer, Part> tail = phrase.parts.tailMap(phrase.nounEndIndex, false);
            for (Integer k : tail.keySet()) {
                if (!GrammarUtils.canBeAdjective(tail.get(k).raw, phrase.gender)) {
                    break;
                }
                phrase.endIndex = k;
            }
            if (!Objects.equals(phrase.endIndex, phrase.nounEndIndex) && !Objects.equals(phrase.endIndex, lastIndex)) {
                // TODO: phrases with two nouns are ignored for now
                phrase.endIndex = phrase.nounEndIndex;
            }
        }

        private static void compileNoun(Assembler phrase) {
            int index = phrase.nounStartIndex;
            phrase.nounEndIndex = index;
            Part noun = phrase.parts.get(index);
            if (!noun.isBlank()) { // already processed
                phrase.fill(noun);
                return;
            }
            findAdnInsertNoun(noun, phrase.gender, phrase.animate);
            phrase.fill(noun);
            if (noun.word != null) {
                return;
            }
            if (!noun.key().contains("-")) {
                if (noun.isBlank()) {
                    noun.fill(phrase.gender, phrase.animate, false);
                }
                return;
            }
            // process with hyphen e.g. "альфа-лучи", "лётчик-наблюдатель", "караван-сарай"
            String[] words = noun.key().split("-");
            Gender g = phrase.gender;
            List<Part> newParts = new ArrayList<>();
            for (int i = 0; i < words.length; i++) {
                Part p = new Part(words[i]);
                findAdnInsertNoun(p, g, phrase.animate);
                if (i > 0 && phrase.gender != Gender.MALE && p.word == null) {
                    // the second part is usually masculine (e.g. "сестра-анестезист")
                    g = Gender.MALE;
                    findAdnInsertNoun(p, g, phrase.animate);
                }
                if (p.isBlank()) {
                    p.fill(g, phrase.animate, false);
                }
                p.space = i == words.length - 1 ? noun.space : "-";
                newParts.add(p);
            }
            phrase.parts.remove(index);
            for (int i = 0; i < newParts.size(); i++) {
                phrase.parts.put(phrase.nounEndIndex = index + i, newParts.get(i));
            }
            phrase.fill(newParts.get(0));
        }

        private static void findAdnInsertNoun(Part part, Gender gender, Boolean animate) {
            Optional<Word> from = fromDictionary(part.key(), gender, animate);
            if (from.isPresent()) {
                part.plural = false;
            } else if (GrammarUtils.canBePlural(part.key())) {
                from = fromDictionary(GrammarUtils.toSingular(part.key()), gender, animate);
                if (from.isPresent()) {
                    part.plural = true;
                }
            }
            from.ifPresent(word -> part.word = word);
            part.indeclinable = from.map(Word::isIndeclinable).orElse(false);
            if (gender == null) {
                gender = from.map(Word::gender).orElseGet(() -> GrammarUtils.guessGenderOfSingularNoun(part.key()));
            }
            if (animate == null) {
                animate = from.map(Word::animate).orElse(null);
            }
            part.gender = gender;
            part.animate = animate;
        }

        static class Part {
            private final String raw;
            String space;
            Word word;
            Gender gender;
            Boolean animate;
            Boolean plural;
            boolean indeclinable;
            private String key;

            Part(String raw) {
                this.raw = Objects.requireNonNull(raw);
            }

            public String key() {
                return key == null ? key = toKey(raw) : key;
            }

            boolean isBlank() {
                return word == null && gender == null && animate == null && !indeclinable;
            }

            void fill(Gender gender, Boolean animate, boolean indeclinable) {
                this.gender = gender;
                this.animate = animate;
                this.indeclinable = indeclinable;
            }

            public Word toWord() {
                return new WordInfo(gender, animate, plural, indeclinable, word);
            }

            @Override
            public String toString() {
                return String.format("'%s'", raw);
            }
        }
    }

    public static class WordInfo implements Word {
        private final Gender gender;
        private final Boolean animated;
        private final Boolean isPlural;
        private final boolean indeclinable;
        private final Word from;

        public WordInfo(Gender gender, Boolean animated, Boolean isPlural, boolean indeclinable, Word from) {
            this.gender = gender;
            this.animated = animated;
            this.isPlural = isPlural;
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

        public Boolean isPlural() {
            return isPlural;
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
            return String.format("Details{gender=%s, animated=%s, is-plural=%s, indeclinable=%s, from=%s}",
                    gender, animated, isPlural, indeclinable, from);
        }
    }
}

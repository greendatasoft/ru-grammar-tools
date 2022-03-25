package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.PartOfSpeech;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.dictionaries.PlainDictionary;
import pro.greendata.rugrammartools.impl.utils.GrammarUtils;
import pro.greendata.rugrammartools.impl.utils.HumanNameUtils;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.util.*;

/**
 * Represents a phrase.
 */
public class Phrase {
    protected final String raw;
    protected final List<String> keys;
    protected final List<String> words;
    protected final List<Word> details;
    protected final List<String> separators;
    protected final Gender gender;
    protected final Boolean animate;
    protected final Boolean plural;

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

    private static Optional<Word> fromDictionary(String key, Gender gender, Boolean animate) {
        return Dictionary.getNounDictionary().wordDetails(key, gender, animate);
    }

    private static String toKey(String w) {
        return TextUtils.normalize(w);
    }

    private static boolean isSpace(char ch) {
        return Character.isWhitespace(ch);
    }

    private static boolean isStopSymbol(char ch) {
        return ch == '\'' || ch == '"' || ch == '\u00AB';
    }

    /**
     * Creates a phrase object from the given string.
     *
     * @param phrase  {@code String}, not {@code null}
     * @param type    {@link Type}
     * @param gender  {@link Gender} (can be {@code null}), filter parameter
     * @param animate {@code Boolean} (can be {@code null}), filter parameter
     * @return {@link Phrase}
     */
    public static Phrase parse(String phrase, Type type, Gender gender, Boolean animate) {
        Assembler res = Assembler.split(phrase);
        if (res.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return res.compile(type, gender, animate).toPhrase();
    }

    /**
     * Makes a mutable copy of this phrase.
     *
     * @return {@link Mutable}
     */
    public Mutable toMutable() {
        return new Mutable(raw, gender, animate, plural, new ArrayList<>(keys), words, details, separators);
    }

    /**
     * Glues the phrase parts back into single {@code String}-phrase.
     *
     * @return a {@code String}
     */
    public String compose() {
        if (separators.size() != keys.size() + 1) {
            throw new IllegalStateException();
        }
        StringBuilder res = new StringBuilder();
        res.append(separators.get(0));
        for (int i = 0; i < keys.size(); i++) {
            res.append(TextUtils.toProperCase(words.get(i), keys.get(i)));
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

    @Override
    public String toString() {
        return String.format("Phrase{raw='%s', words=%s}", raw, keys);
    }

    /**
     * A phrase type.
     */
    public enum Type {
        PROFESSION_NAME,
        ORGANIZATION_NAME,
        ANY
    }

    /**
     * Mutable {@link Phrase} impl.
     */
    public static class Mutable extends Phrase {

        protected Mutable(String phrase,
                          Gender gender,
                          Boolean animate,
                          Boolean plural,
                          List<String> keys,
                          List<String> words,
                          List<Word> details,
                          List<String> separators) {
            super(phrase, gender, animate, plural, keys, words, details, separators);
        }

        /**
         * Changes the text for the given index.
         *
         * @param i   {@code int}, index of word
         * @param txt {@code String} new text
         */
        public void set(int i, String txt) {
            keys.set(i, Objects.requireNonNull(txt));
        }

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
        // end of the declinable part of phrase
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
         * If the phrase contains {@link Phrase#isStopSymbol(char) stop symbol},
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
                    separator.append(chars, j + 1, chars.length - j - 1);
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

        /**
         * Compiles this object collecting its content.
         *
         * @param type {@link Phrase.Type} TODO: handle phrase type
         * @param inputGender  {@link Gender}, can be {@code null}
         * @param inputAnimate {@code Boolean}, can be {@code null}
         * @return this instance
         */
        Assembler compile(Type type, Gender inputGender, Boolean inputAnimate) {
            fill(inputGender, inputAnimate);

            if (PlainDictionary.NON_DERIVATIVE_PREPOSITION.contains(this.parts.firstEntry().getValue().key())) {
                // starts with preposition -> consider the whole phrase as indeclinable
                this.parts.forEach((i, p) -> p.fillSettings(gender, null, animate, true));
                return this;
            }

            findAndSetNounPosition(this);
            compileNoun(this);
            findAndSetEndPosition(this);

            this.parts.headMap(this.endIndex, true).forEach((i, p) -> {
                if (p.isBlank()) {
                    p.fillSettings(gender, PartOfSpeech.ADJECTIVE, animate, false);
                }
            });
            this.parts.tailMap(this.endIndex, false)
                    .forEach((i, p) -> p.fillSettings(gender, PartOfSpeech.ADJECTIVE, animate, true));
            return this;
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
            Part noun = nounStartIndex == null ? null : Objects.requireNonNull(parts.get(nounStartIndex));
            parts.forEach((index, part) -> {
                if (!Objects.equals(index, last)) {
                    separators.add(Objects.requireNonNull(part.space));
                }
                words.add(part.raw);
                keys.add(part.key());
                details.add(part.toWord());
            });
            separators.add(trailingSpace == null ? "" : trailingSpace);

            return new Phrase(raw, this.gender, this.animate, noun == null ? null : noun.plural,
                    Collections.unmodifiableList(keys), Collections.unmodifiableList(words),
                    Collections.unmodifiableList(details), Collections.unmodifiableList(separators));
        }

        public boolean isEmpty() {
            return parts.isEmpty();
        }

        private boolean isNullOr(Gender g) {
            return gender == null || gender == g;
        }

        private void fillFromWord(Part p) {
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
                if (GrammarUtils.canBeAbbreviation(w, phrase.raw)) {
                    part.fillSettings(phrase.gender, PartOfSpeech.NOUN, phrase.animate, true);
                    if (GrammarUtils.canBeHumanRelatedAbbreviation(w) && next != null) { // e.g. "ИП Иванов"
                        if (handleHumanName(phrase, next.getKey(), true)) {
                            break;
                        }
                    }
                    // e.g. "ПАО 'Финансовая корпорация'"
                    phrase.nounStartIndex = index;
                    break;
                }
                if (phrase.animate != Boolean.FALSE && handleHumanName(phrase, index, false)) {
                    break;
                }
                // the following code was designed for inflection profession names
                // if the next word is preposition then the first word can be noun (e.g. "Термист по обработке слюды")
                if (next != null && PlainDictionary.NON_DERIVATIVE_PREPOSITION.contains(next.getValue().key())) {
                    next.getValue().indeclinable = true;
                    phrase.nounStartIndex = index;
                    break;
                }
                // (masculine) skip leading adjectives
                if (phrase.isNullOr(Gender.MALE) && GrammarUtils.canBeSingularNominativeMasculineAdjective(w)) {
                    phrase.gender = Gender.MALE;
                    if (GrammarUtils.canBeMasculineAdjectiveBasedSubstantiveNoun(w)) {
                        phrase.nounStartIndex = index;
                        break;
                    }
                    continue;
                }
                // (feminine) skip the leading adjectives
                if (phrase.isNullOr(Gender.FEMALE) && GrammarUtils.canBeSingularNominativeFeminineAdjective(w)) {
                    phrase.gender = Gender.FEMALE;
                    if (GrammarUtils.canBeFeminineAdjectiveBasedSubstantiveNoun(w)) {
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

        private static boolean handleHumanName(Assembler phrase, Integer index, boolean sureIsName) {
            Part current = phrase.parts.get(index);
            if (current == null) {
                return false;
            }
            Map.Entry<Integer, Part> nextWord = phrase.parts.higherEntry(index);
            if (sureIsName && nextWord == null && HumanNameUtils.canBeSurname(current.raw)) { // e.g. "Петрова"
                phrase.nounStartIndex = index;
                handleSurname(phrase, current);
                return true;
            }
            if (nextWord == null) {
                return false;
            }
            Part next = nextWord.getValue();
            Integer nextIndex = nextWord.getKey();
            if (HumanNameUtils.canBeInitials(current.raw) && HumanNameUtils.canBeSurname(next.raw)) { // e.g. "П.П. Петрова"
                phrase.nounStartIndex = nextIndex;
                handleSurname(phrase, next);
                return true;
            }
            if (HumanNameUtils.canBeInitials(next.raw) && HumanNameUtils.canBeSurname(current.raw)) { // e.g. "Петров П.П."
                phrase.nounStartIndex = index;
                handleSurname(phrase, current);
                return true;
            }
            List<Part> sfp = new ArrayList<>(3);
            NavigableMap<Integer, Part> rest = phrase.parts.tailMap(nextIndex, false);
            Part nextNext = null;
            Integer nextNextIndex = null;
            if (!rest.isEmpty()) {
                nextNext = rest.firstEntry().getValue();
                nextNextIndex = rest.firstEntry().getKey();
            }
            if (HumanNameUtils.isFirstname(current.raw)) { // e.g. "Полина Петровна Петрова" or "Полина Петрова"
                if (nextNext != null && HumanNameUtils.canBePatronymic(next.raw) && HumanNameUtils.canBeSurname(nextNext.raw)) {
                    sfp.add(nextNext);
                    sfp.add(current);
                    sfp.add(next);
                } else if (HumanNameUtils.canBeSurname(next.raw)) {
                    sfp.add(next);
                    sfp.add(next);
                }
            } else if (HumanNameUtils.isFirstname(next.raw)) { // e.g. "Петров Петр Петрович" or "Петрова Полина"
                if (HumanNameUtils.canBeSurname(current.raw)) {
                    if (nextNext != null && HumanNameUtils.canBePatronymic(nextNext.raw)) {
                        sfp.add(current);
                        sfp.add(next);
                        sfp.add(nextNext);
                    } else {
                        sfp.add(current);
                        sfp.add(next);
                    }
                }
            }
            if (sfp.isEmpty()) {
                return false;
            }
            Gender gender = HumanNameUtils.guessGenderByFullName(sfp.stream().map(x -> x.raw).toArray(String[]::new));
            if (gender == null) {
                return false;
            }
            sfp.get(0).gender = gender;
            sfp.get(0).animate = true;
            sfp.get(0).type = RuleType.FAMILY_NAME;
            phrase.fillFromWord(sfp.get(0));
            sfp.get(1).gender = gender;
            sfp.get(1).animate = true;
            sfp.get(1).type = RuleType.FIRST_NAME;
            if (sfp.size() > 2) {
                sfp.get(2).gender = gender;
                sfp.get(2).animate = true;
                sfp.get(2).type = RuleType.PATRONYMIC_NAME;
            }
            phrase.nounStartIndex = nextNextIndex != null ? nextNextIndex : nextIndex;
            return true;
        }

        private static void handleSurname(Assembler phrase, Part next) {
            next.type = RuleType.FAMILY_NAME;
            next.animate = true;
            next.gender = HumanNameUtils.canBeFemaleSurname(next.raw) ? Gender.FEMALE : Gender.MALE;
            phrase.fillFromWord(next);
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
                phrase.fillFromWord(noun);
                return;
            }
            findAdnInsertNoun(noun, phrase.gender, phrase.animate);
            phrase.fillFromWord(noun);
            if (noun.word != null) {
                return;
            }
            if (!noun.key().contains("-")) {
                if (noun.isBlank()) {
                    noun.fillSettings(phrase.gender, PartOfSpeech.NOUN, phrase.animate, false);
                }
                return;
            }
            // process with hyphen e.g. "альфа-лучи", "лётчик-наблюдатель", "караван-сарай"
            String[] words = noun.raw.split("-");
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
                    p.fillSettings(g, PartOfSpeech.NOUN, phrase.animate, false);
                }
                p.space = i == words.length - 1 ? noun.space : "-";
                newParts.add(p);
            }
            phrase.parts.remove(index);
            for (int i = 0; i < newParts.size(); i++) {
                phrase.parts.put(phrase.nounEndIndex = index + i, newParts.get(i));
            }
            phrase.fillFromWord(newParts.get(0));
        }

        private static void findAdnInsertNoun(Part part, Gender gender, Boolean animate) {
            Optional<Word> from = fromDictionary(part.key(), gender, animate);
            if (from.isPresent()) {
                part.plural = false;
            } else if (GrammarUtils.canBePlural(part.key())) {
                String k = GrammarUtils.toSingular(part.key());
                from = fromDictionary(k, gender, animate);
                if (from.isPresent()) {
                    part.key = k; // replace key!
                    part.plural = true;
                }
            }
            from.ifPresent(word -> part.word = word);
            part.indeclinable = from.map(Word::isIndeclinable).orElse(false);
            if (gender == null) {
                gender = from.map(Word::gender).orElseGet(() -> GrammarUtils.guessGenderOfSingularNoun(part.key()));
                if (gender == null) {
                    String k = GrammarUtils.toSingular(part.key());
                    gender = GrammarUtils.guessGenderOfSingularNoun(k);
                    if (gender != null) {
                        part.key = k; // replace key!
                        part.plural = true; // possible wrong
                    } else {
                        // the masculine gender is most common (in russian job-titles)
                        gender = Gender.MALE;
                    }
                }
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
            RuleType type = RuleType.GENERIC;
            PartOfSpeech partOfSpeech;
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
                return word == null && gender == null && partOfSpeech == null && animate == null && !indeclinable;
            }

            void fillSettings(Gender gender, PartOfSpeech partOfSpeech, Boolean animate, boolean indeclinable) {
                this.gender = gender;
                this.partOfSpeech = partOfSpeech;
                this.animate = animate;
                this.indeclinable = indeclinable;
            }

            public Word toWord() {
                return new WordInfo(type, gender, partOfSpeech, animate, plural, indeclinable, word);
            }

            @Override
            public String toString() {
                return String.format("'%s'", raw);
            }
        }
    }

    /**
     * A container to hold word details.
     */
    public static class WordInfo implements Word {
        private final Gender gender;
        private final Boolean animate;
        private final PartOfSpeech partOfSpeech;
        private final RuleType ruleType;
        private final Boolean isPlural;
        private final boolean indeclinable;
        private final Word from;

        public WordInfo(RuleType ruleType,
                        Gender gender,
                        PartOfSpeech partOfSpeech,
                        Boolean animate,
                        Boolean isPlural,
                        boolean indeclinable,
                        Word from) {
            this.ruleType = ruleType;
            this.gender = gender;
            this.partOfSpeech = partOfSpeech;
            this.animate = animate;
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
            return animate;
        }

        @Override
        public PartOfSpeech partOfSpeech() {
            return partOfSpeech;
        }

        public Boolean isPlural() {
            return isPlural;
        }

        @Override
        public boolean isIndeclinable() {
            return indeclinable;
        }

        @Override
        public RuleType rule() {
            return ruleType;
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
            return String.format("Details{gender=%s, pos=%s, animated=%s, is-plural=%s, indeclinable=%s, from=%s}",
                    gender, partOfSpeech, animate, isPlural, indeclinable, from);
        }
    }
}

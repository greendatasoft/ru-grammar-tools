package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.dictionaries.NounDictionary;
import pro.greendata.rugrammartools.impl.dictionaries.PlainDictionary;
import pro.greendata.rugrammartools.impl.utils.GrammarUtils;
import pro.greendata.rugrammartools.impl.utils.HumanNameUtils;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.util.*;

/**
 * Mutable phrase-builder.
 */
public class PhraseAssembler {
    private static final int INDEX_STEP = 1000;

    private final NavigableMap<Integer, Part> parts = new TreeMap<>();
    // input phrase
    private String raw;
    // the position of the main noun (subject) in the phrase
    private Integer subjectStartIndex;
    // for composed nouns end != start (e.g. "сестра-анестезист")
    private Integer subjectEndIndex;
    // end of the declinable part of phrase
    private Integer endIndex;
    // usually gender is null, it is determined by the phrase;
    // may not match the true gender of the wearer (in case of profession).
    private Gender phraseGender;
    // can be null
    private Boolean phraseAnimate;
    private String leadingSpace;
    private String trailingSpace;

    public static Optional<NounDictionary.Word> fromDictionary(String key, Gender gender, Boolean animate) {
        return Dictionary.getNounDictionary().wordDetails(key, gender, animate);
    }

    public static String toKey(String w) {
        return TextUtils.normalize(w);
    }

    public static boolean isSpace(char ch) {
        return Character.isWhitespace(ch);
    }

    public static boolean isStopSymbol(char ch) {
        return ch == '\'' || ch == '"' || ch == '\u00AB';
    }

    /**
     * Splits the given {@code phrase} into parts wrapped as {@link PhraseAssembler} object.
     * If the phrase contains {@link PhraseAssembler#isStopSymbol(char) stop symbol},
     * then the rest part of phrase after that symbol inclusively is considered as indeclinable.
     *
     * @param phrase {@code String}, not {@code null}
     * @return {@link PhraseAssembler}
     */
    public static PhraseAssembler split(String phrase) {
        PhraseAssembler res = new PhraseAssembler();
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
     * @param type         {@link Phrase.Type} TODO: handle phrase type
     * @param inputGender  {@link Gender}, can be {@code null}
     * @param inputAnimate {@code Boolean}, can be {@code null}
     * @return this instance
     */
    public PhraseAssembler compile(Phrase.Type type, Gender inputGender, Boolean inputAnimate) {
        fillMissedSettings(inputGender, inputAnimate);

        if (PlainDictionary.NON_DERIVATIVE_PREPOSITION.contains(this.parts.firstEntry().getValue().key())) {
            // starts with preposition -> consider the whole phrase as indeclinable
            this.parts.forEach((i, p) -> p.fillMissedSettings(phraseGender, null, phraseAnimate, true));
            return this;
        }

        processPreSubjectParts(this);
        processSubject(this);
        processPostSubjectParts(this);

        // set default settings for adjectives surrounding the subject
        this.parts.headMap(this.endIndex, true)
                .forEach((i, p) -> p.fillMissedSettings(phraseGender, null, phraseAnimate, false));
        // the rest of the phrase (supplemental part) is indeclinable
        this.parts.tailMap(this.endIndex, false)
                .forEach((i, p) -> p.fillMissedSettings(phraseGender, null, phraseAnimate, true));
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
        Part noun = subjectStartIndex == null ? null : Objects.requireNonNull(parts.get(subjectStartIndex));
        parts.forEach((index, part) -> {
            if (!Objects.equals(index, last)) {
                separators.add(Objects.requireNonNull(part.space));
            }
            words.add(part.raw);
            keys.add(part.key());
            details.add(part.toWord());
        });
        separators.add(trailingSpace == null ? "" : trailingSpace);

        return new Phrase(raw, this.phraseGender, this.phraseAnimate, noun == null ? null : noun.plural,
                Collections.unmodifiableList(keys), Collections.unmodifiableList(words),
                Collections.unmodifiableList(details), Collections.unmodifiableList(separators));
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    private boolean isNullOr(Gender g) {
        return phraseGender == null || phraseGender == g;
    }

    private void fillMissedSettingsFromWord(Part word) {
        fillMissedSettings(word.gender, word.animate);
    }

    private void fillMissedSettings(Gender g, Boolean a) {
        if (this.phraseAnimate == null) {
            this.phraseAnimate = a;
        }
        if (this.phraseGender == null) {
            this.phraseGender = g;
        }
    }

    private static void processPreSubjectParts(PhraseAssembler phrase) {
        phrase.subjectStartIndex = phrase.parts.firstKey(); // the position of the main word in the phrase
        for (Integer index : phrase.parts.keySet()) {
            Part part = phrase.parts.get(index);
            String w = part.raw;
            if (!GrammarUtils.isRuWord(w)) {
                part.indeclinable = true;
                continue;
            }
            Map.Entry<Integer, Part> next = phrase.parts.higherEntry(index);
            if (GrammarUtils.canBeAbbreviation(w, phrase.raw)) {
                part.fillMissedSettings(phrase.phraseGender, PartOfSpeech.NOUN, phrase.phraseAnimate, true);
                if (GrammarUtils.canBeHumanRelatedAbbreviation(w) && next != null) { // e.g. "ИП Иванов"
                    if (handleHumanName(phrase, next.getKey(), true)) {
                        break;
                    }
                }
                // e.g. "ПАО 'Финансовая корпорация'"
                phrase.subjectStartIndex = index;
                break;
            }
            if (phrase.phraseAnimate != Boolean.FALSE && handleHumanName(phrase, index, false)) {
                break;
            }
            // the following code was designed for inflection profession names
            // if the next word is preposition then the first word can be noun (e.g. "Термист по обработке слюды")
            if (next != null && PlainDictionary.NON_DERIVATIVE_PREPOSITION.contains(next.getValue().key())) {
                next.getValue().fillMissedSettings(null, PartOfSpeech.PREPOSITION, null, true);
                phrase.subjectStartIndex = index;
                break;
            }
            // (masculine) skip leading adjectives
            if (phrase.isNullOr(Gender.MALE) && GrammarUtils.canBeSingularNominativeMasculineAdjective(w)) {
                phrase.phraseGender = Gender.MALE;
                if (GrammarUtils.canBeMasculineAdjectiveBasedSubstantiveNoun(w)) {
                    phrase.subjectStartIndex = index;
                    break;
                }
                part.partOfSpeech = PartOfSpeech.ADJECTIVE;
                continue;
            }
            // (feminine) skip the leading adjectives
            if (phrase.isNullOr(Gender.FEMALE) && GrammarUtils.canBeSingularNominativeFeminineAdjective(w)) {
                phrase.phraseGender = Gender.FEMALE;
                if (GrammarUtils.canBeFeminineAdjectiveBasedSubstantiveNoun(w)) {
                    phrase.subjectStartIndex = index;
                    break;
                }
                part.partOfSpeech = PartOfSpeech.ADJECTIVE;
                continue;
            }
            // (neuter) skip leading adjectives
            if (phrase.isNullOr(Gender.NEUTER) && GrammarUtils.canBeSingularNominativeNeuterAdjective(w)) {
                phrase.phraseGender = Gender.NEUTER;
                part.partOfSpeech = PartOfSpeech.ADJECTIVE;
                continue;
            }
            phrase.subjectStartIndex = index;
            break;
        }
    }

    private static void processPostSubjectParts(PhraseAssembler phrase) {
        phrase.endIndex = phrase.subjectEndIndex;
        Integer lastIndex = phrase.parts.lastKey();
        NavigableMap<Integer, Part> tail = phrase.parts.tailMap(phrase.subjectEndIndex, false);
        for (Integer k : tail.keySet()) {
            Part p = tail.get(k);
            if (!GrammarUtils.canBeAdjective(p.raw, phrase.phraseGender)) {
                break;
            }
            phrase.endIndex = k;
        }
        if (!Objects.equals(phrase.endIndex, phrase.subjectEndIndex) && !Objects.equals(phrase.endIndex, lastIndex)) {
            // TODO: phrases with two nouns are ignored for now
            phrase.endIndex = phrase.subjectEndIndex;
        }
    }

    private static void processSubject(PhraseAssembler phrase) {
        int index = phrase.subjectStartIndex;
        phrase.subjectEndIndex = index;
        Part subject = phrase.parts.get(index);
        processNoun(subject, phrase.phraseGender, phrase.phraseAnimate);
        if (subject.word != null) {
            phrase.fillMissedSettingsFromWord(subject);
            return;
        }
        if (!subject.key().contains("-")) {
            phrase.fillMissedSettingsFromWord(subject);
            return;
        }
        // process with hyphen e.g. "альфа-лучи", "лётчик-наблюдатель", "караван-сарай"
        String[] words = subject.raw.split("-");
        Gender g = phrase.phraseGender;
        List<Part> newParts = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            Part p = new Part(words[i]);
            processNoun(p, g, phrase.phraseAnimate);
            if (i > 0 && phrase.phraseGender != Gender.MALE && p.word == null) {
                // the second part is usually masculine (e.g. "сестра-анестезист")
                g = Gender.MALE;
                processNoun(p, g, phrase.phraseAnimate);
            }
            p.space = i == words.length - 1 ? subject.space : "-";
            newParts.add(p);
        }
        phrase.parts.remove(index);
        for (int i = 0; i < newParts.size(); i++) {
            phrase.parts.put(phrase.subjectEndIndex = index + i, newParts.get(i));
        }
        phrase.fillMissedSettingsFromWord(newParts.get(0));
    }

    private static void processNoun(Part part, Gender gender, Boolean animate) {
        Optional<? extends Dictionary.Record> from = findNounInDictionary(part, gender, animate);
        if (from.isPresent()) {
            return;
        }
        // if the noun is not found in the dictionary,
        // then derive its settings from the input parameters and semantic of the word itself
        if (part.indeclinable == null) { // default value:
            part.indeclinable = false;
        } else if (part.indeclinable) { // skip indeclinable
            return;
        }
        if (gender == null) {
            gender = GrammarUtils.guessGenderOfSingularNoun(part.key());
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
        part.gender = gender;
        part.animate = animate;
    }

    private static Optional<? extends Dictionary.Record> findNounInDictionary(Part part, Gender givenGender, Boolean givenAnimate) {
        if (part.word != null) { // already processed, found
            return Optional.of(part.word);
        }
        if (part.notFoundInDictionary) { // already processed, but not found
            return Optional.empty();
        }
        Optional<NounDictionary.Word> from = fromDictionary(part.key(), givenGender, givenAnimate);
        if (from.isPresent()) {
            part.plural = false;
        } else if (GrammarUtils.canBePlural(part.key())) {
            String k = GrammarUtils.toSingular(part.key());
            from = fromDictionary(k, givenGender, givenAnimate);
            if (from.isPresent()) {
                part.key = k; // replace key!
                part.plural = true;
            }
        }
        part.notFoundInDictionary = from.isEmpty();
        from.ifPresent(word -> {
            part.word = word;
            Gender g = Optional.ofNullable(word.gender()).orElse(givenGender);
            Boolean a = Optional.ofNullable(word.animate()).orElse(givenAnimate);
            part.fillMissedSettings(g, PartOfSpeech.NOUN, a, word.isIndeclinable());
        });
        return from;
    }

    private static boolean handleHumanName(PhraseAssembler phrase, Integer index, boolean sureIsName) {
        Part current = phrase.parts.get(index);
        if (current == null) {
            return false;
        }
        Map.Entry<Integer, Part> nextWord = phrase.parts.higherEntry(index);
        if (sureIsName && nextWord == null && HumanNameUtils.canBeSurname(current.raw)) { // e.g. "Петрова"
            phrase.subjectStartIndex = index;
            handleSurname(phrase, current);
            return true;
        }
        if (nextWord == null) {
            return false;
        }
        Part next = nextWord.getValue();
        Integer nextIndex = nextWord.getKey();
        if (HumanNameUtils.canBeInitials(current.raw) && HumanNameUtils.canBeSurname(next.raw)) { // e.g. "П.П. Петрова"
            phrase.subjectStartIndex = nextIndex;
            handleSurname(phrase, next);
            return true;
        }
        if (HumanNameUtils.canBeInitials(next.raw) && HumanNameUtils.canBeSurname(current.raw)) { // e.g. "Петров П.П."
            phrase.subjectStartIndex = index;
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
        phrase.fillMissedSettingsFromWord(sfp.get(0));
        sfp.get(1).gender = gender;
        sfp.get(1).animate = true;
        sfp.get(1).type = RuleType.FIRST_NAME;
        if (sfp.size() > 2) {
            sfp.get(2).gender = gender;
            sfp.get(2).animate = true;
            sfp.get(2).type = RuleType.PATRONYMIC_NAME;
        }
        phrase.subjectStartIndex = nextNextIndex != null ? nextNextIndex : nextIndex;
        return true;
    }

    private static void handleSurname(PhraseAssembler phrase, Part next) {
        next.type = RuleType.FAMILY_NAME;
        next.animate = true;
        next.gender = HumanNameUtils.canBeFemaleSurname(next.raw) ? Gender.FEMALE : Gender.MALE;
        phrase.fillMissedSettingsFromWord(next);
    }

    static class Part {
        private final String raw;
        String space;
        Dictionary.Record word;
        Gender gender;
        Boolean animate;
        RuleType type = RuleType.GENERIC;
        PartOfSpeech partOfSpeech;
        Boolean plural;
        Boolean indeclinable;
        boolean notFoundInDictionary;
        private String key;

        Part(String raw) {
            this.raw = Objects.requireNonNull(raw);
        }

        public String key() {
            return key == null ? key = toKey(raw) : key;
        }

        void fillMissedSettings(Gender gender, PartOfSpeech partOfSpeech, Boolean animate, boolean indeclinable) {
            if (this.gender == null) {
                this.gender = gender;
            }
            if (this.partOfSpeech == null) {
                this.partOfSpeech = partOfSpeech;
            }
            if (this.animate == null) {
                this.animate = animate;
            }
            if (this.indeclinable == null) {
                this.indeclinable = indeclinable;
            }
        }

        public Word toWord() {
            return new Phrase.WordInfo(type, gender, partOfSpeech, animate, plural, indeclinable, word);
        }

        @Override
        public String toString() {
            return String.format("'%s'", raw);
        }
    }
}

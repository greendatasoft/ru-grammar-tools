package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.impl.dictionaries.Dictionary;
import pro.greendata.rugrammartools.impl.dictionaries.NounDictionary;
import pro.greendata.rugrammartools.impl.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        PhraseAssembler res = PhraseAssembler.split(phrase);
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
     * A container to hold word details.
     */
    public static class WordInfo implements Word {
        private final Gender gender;
        private final Boolean animate;
        private final PartOfSpeech partOfSpeech;
        private final RuleType ruleType;
        private final Boolean isPlural;
        private final boolean indeclinable;
        private final NounDictionary.Record from;

        public WordInfo(RuleType ruleType,
                        Gender gender,
                        PartOfSpeech partOfSpeech,
                        Boolean animate,
                        Boolean isPlural,
                        boolean indeclinable,
                        NounDictionary.Record from) {
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

        @Override
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
        public Dictionary.Record record() {
            return from;
        }

        @Override
        public String toString() {
            return String.format("Details{gender=%s, pos=%s, animated=%s, is-plural=%s, indeclinable=%s, from=%s}",
                    gender, partOfSpeech, animate, isPlural, indeclinable, from);
        }
    }
}

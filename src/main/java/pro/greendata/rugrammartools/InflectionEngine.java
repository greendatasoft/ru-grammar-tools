package pro.greendata.rugrammartools;

/**
 * An engine for running inflection process.
 * Created by @ssz on 27.11.2020.
 */
public interface InflectionEngine {
    /**
     * Declines the given {@code word} in accordance with the specified settings.
     * This is the generic method.
     *
     * @param word       {@code String}, single word or phrase, a term, in nominative case, not {@code null}
     * @param type       {@link WordType}, not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender} feminine, masculine or neuter,
     *                   {@code null} to choose automatically (usually it is {@link Gender#MALE}) or for undefined cases
     * @param animate    {@code Boolean} can be specified if {@code type = } {@link WordType#GENERIC},
     *                   {@code null} for default behaviour
     * @param plural     {@code Boolean} if {@code true} then plural,
     *                   {@code false} for singular or {@code null} for default behaviour,
     *                   makes sense only if {@code type = } {@link WordType#GENERIC}
     * @return {@code String}
     */
    String inflect(String word, WordType type, Case declension, Gender gender, Boolean animate, Boolean plural);

    /**
     * Declines the given {@code phrase} (combination of words: job-title, organization name)
     * into the specified declension case.
     *
     * @param phrase     {@code String} not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param animate    {@code Boolean} the names of organizations are usually inanimate, the names of professions are animate
     * @return {@code String} -  a phrase in the selected case
     */
    default String inflectRegularTerm(String phrase, Case declension, Boolean animate) {
        // this is the default rule, which works only for several simplest cases
        return inflect(phrase, WordType.GENERIC, declension, Gender.MALE, animate, false);
    }

    /**
     * Declines the given {@code numeral} with the {@code unit} into the specified declension case.
     *
     * @param numeral    {@code String} not {@code null},
     *                   a singular quantitative numeral in nominative case, e.g.{@code "пять"}
     *                   or a singular male ordinal numeral in nominative case, e.g. {@code "четвёртый"}
     * @param unit       {@code String} not {@code null}, a noun in singular nominative case, e.g.{@code "заяц"}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a phrase in the selected case
     * @see SpellingEngine#spell(java.math.BigDecimal)
     * @see SpellingEngine#spellOrdinal(java.math.BigInteger, Gender)
     */
    default String inflectNumeral(String numeral, String unit, Case declension) {
        // this is the default rule, it works only for several cases
        return inflectNumeral(numeral, declension) + " " +
                inflect(unit, WordType.GENERIC, Case.GENITIVE, Gender.MALE, null, null);
    }

    /**
     * Declines the given {@code numeral} into the specified declension case.
     * A {@code numeral} is considered as inanimate.
     *
     * @param numeral    {@code String} not {@code null},
     *                   a quantitative numeral e.g. {@code "пятьсот"}
     *                   or ordinal numeral {@code "первая"}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a numeral phrase in the selected case
     * @see SpellingEngine#spell(java.math.BigDecimal)
     * @see SpellingEngine#spellOrdinal(java.math.BigInteger, Gender)
     */
    default String inflectNumeral(String numeral, Case declension) {
        // this is default rule, which works only for several cases
        return inflect(numeral, WordType.NUMERAL, declension, null, null, null);
    }

    /**
     * Declines the given {@code firstname} into the specified declension case.
     *
     * @param firstname  {@code String}, not {@code null}, e.g. {@code "Петра"}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender#MALE masculine} or {@link Gender#FEMALE feminine},
     *                   or {@code null} to choose automatically or for undefined cases
     * @return {@code String} the firstname in the selected case
     */
    default String inflectFirstname(String firstname, Case declension, Gender gender) {
        return inflect(firstname, WordType.FIRST_NAME, declension, gender, true, false);
    }

    /**
     * Declines the given {@code middle} into the specified declension case.
     *
     * @param middlename {@code String}, not {@code null}, e.g. {@code "Петровна"}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender#MALE masculine} or {@link Gender#FEMALE feminine},
     *                   or {@code null} to choose automatically or for undefined cases
     * @return {@code String} the middlename in the selected case
     */
    default String inflectPatronymic(String middlename, Case declension, Gender gender) {
        return inflect(middlename, WordType.PATRONYMIC_NAME, declension, gender, true, false);
    }

    /**
     * Declines the given {@code surname} into the specified declension case.
     *
     * @param surname    {@code String}, not {@code null}, e.g. {@code "Петрова"}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender#MALE masculine} or {@link Gender#FEMALE feminine},
     *                   or {@code null} to choose automatically or for undefined cases
     * @return {@code String} the surname in the selected case
     */
    default String inflectSurname(String surname, Case declension, Gender gender) {
        return inflect(surname, WordType.FAMILY_NAME, declension, gender, true, false);
    }

    /**
     * Declines the given {@code sfp} (full name) into the specified declension case.
     *
     * @param sfp        {@code String} - surname+firstname+patronymic with space as separator, surname is mandatory
     * @param declension {@link Case declension case}, not {@code null}
     * @return surname+firstname+patronymic in desired declension case
     */
    default String inflectFullName(String sfp, Case declension) {
        return String.join(" ", inflectSPF(sfp.split("\\s+"), declension, null));
    }

    /**
     * Declines the given {@code sfp} (full name) into the specified declension case.
     *
     * @param sfp        an {@code Array} with full name: either {@code [surname]} (e.g. {@code "Петров"}),
     *                   or {@code [surname, firstname]} (e.g. {@code "Петров Петр"}),
     *                   or {@code [surname, firstname, patronymic]} (e.g. {@code "Петров Петр Петрович"})
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender} or {@code null} to guess
     * @return {@code Array} with full name in desired declension case
     */
    default String[] inflectSPF(String[] sfp, Case declension, Gender gender) {
        if (sfp.length > 3 || sfp.length == 0) {
            throw new IllegalArgumentException();
        }
        String s = inflectSurname(sfp[0], declension, gender);
        if (sfp.length == 1) {
            return new String[]{s};
        }
        String f = inflectFirstname(sfp[1], declension, gender);
        if (sfp.length == 2) {
            return new String[]{s, f};
        }
        String p = inflectPatronymic(sfp[2], declension, gender);
        return new String[]{s, f, p};
    }

    /**
     * Declines the given {@code phrase} into the specified declension case, guessing the phrase word type.
     * Since need to guess, the accuracy of this method is less than others.
     *
     * @param phrase     {@code String} a phrase: fullname, profession, organization, etc
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} -  a phrase in the selected case
     */
    default String inflectAny(String phrase, Case declension) {
        // this is default rule, which works only for several cases
        if (phrase.split("\\s+").length < 4) {
            return inflectFullName(phrase, declension);
        }
        return inflectRegularTerm(phrase, declension, null);
    }
}

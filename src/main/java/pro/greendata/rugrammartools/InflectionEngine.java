package pro.greendata.rugrammartools;

/**
 * An engine for running inflection process.
 * Created by @ssz on 27.11.2020.
 */
public interface InflectionEngine {

    /**
     * Declines the given {@code phrase} (combination of words: job-title, organization name)
     * into the specified declension case.
     *
     * @param phrase     {@code String} not {@code null}
     * @param declension {@link Case declension case}, not {@code null}
     * @param animate    {@code Boolean} the names of organizations are usually inanimate, the names of professions are animate
     * @return {@code String} -  a phrase in the selected case
     */
    String inflectRegularTerm(String phrase, Case declension, Boolean animate);

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
    String inflectNumeral(String numeral, String unit, Case declension);

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
    String inflectNumeral(String numeral, Case declension);

    /**
     * Declines the given {@code firstname} into the specified declension case.
     *
     * @param firstname  {@code String}, not {@code null}, e.g. {@code "Петра"}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender#MALE masculine} or {@link Gender#FEMALE feminine},
     *                   or {@code null} to choose automatically or for undefined cases
     * @return {@code String} the firstname in the selected case
     */
    String inflectFirstname(String firstname, Case declension, Gender gender);

    /**
     * Declines the given {@code middle} into the specified declension case.
     *
     * @param middlename {@code String}, not {@code null}, e.g. {@code "Петровна"}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender#MALE masculine} or {@link Gender#FEMALE feminine},
     *                   or {@code null} to choose automatically or for undefined cases
     * @return {@code String} the middlename in the selected case
     */
    String inflectPatronymic(String middlename, Case declension, Gender gender);

    /**
     * Declines the given {@code surname} into the specified declension case.
     *
     * @param surname    {@code String}, not {@code null}, e.g. {@code "Петрова"}
     * @param declension {@link Case declension case}, not {@code null}
     * @param gender     {@link Gender#MALE masculine} or {@link Gender#FEMALE feminine},
     *                   or {@code null} to choose automatically or for undefined cases
     * @return {@code String} the surname in the selected case
     */
    String inflectSurname(String surname, Case declension, Gender gender);

    /**
     * Declines the given (legal) {@code profession} name into the specified declension case.
     *
     * @param profession {@code String}, not {@code null}, e.g. {@code "медицинская сестра-анестезист"}
     * @param declension {@link Case declension case}, not {@code null}
     * @return {@code String} the profession name in the selected case
     */
    default String inflectNameOfProfession(String profession, Case declension) {
        return inflectRegularTerm(profession, declension, true);
    }

    /**
     * Declines the given (legal) {@code organization} name into the specified declension case.
     *
     * @param organization {@code String}, not {@code null}, e.g. {@code "акционерное общество"}
     * @param declension   {@link Case declension case}, not {@code null}
     * @return {@code String} the organization name in the selected case
     */
    default String inflectNameOfOrganization(String organization, Case declension) {
        return inflectRegularTerm(organization, declension, false);
    }

    /**
     * Declines the given {@code sfp} (full name) into the specified declension case.
     *
     * @param sfp        {@code String} - surname+firstname+patronymic with space as separator, surname is mandatory
     * @param declension {@link Case declension case}, not {@code null}
     * @return surname+firstname+patronymic in desired declension case
     */
    default String inflectFullname(String sfp, Case declension) {
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
        // this is the default rule that only works in a few cases
        if (phrase.split("\\s+").length < 4) {
            return inflectFullname(phrase, declension);
        }
        return inflectRegularTerm(phrase, declension, null);
    }
}

package com.gitlab.sszuev.inflector.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitlab.sszuev.inflector.Gender;
import com.gitlab.sszuev.inflector.impl.beans.NameBean;
import com.gitlab.sszuev.inflector.impl.beans.RuleBean;
import com.gitlab.sszuev.inflector.impl.beans.RulesBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Rules.
 * Created by @ssz on 02.12.2020.
 *
 * @see <a href='https://github.com/petrovich/petrovich-rules'>petrovich-rules</a>
 * @see <a href='https://github.com/petrovich4j/petrovich4j/blob/master/src/main/java/com/github/petrovich4j/Library.java'>com.github.petrovich4j.Library</a>
 */
public class RuleLibrary {
    private static final RulesBean NAMES_RULES_LIB = loadNamesBean();
    private static final NameBean REGULAR_RULES_LIB = loadRegularBean();

    static final char KEEP_CHARACTER = '.';
    static final char REMOVE_CHARACTER = '-';
    static final String KEEP_MOD = Character.toString(KEEP_CHARACTER);

    public static final RuleSet LAST_NAME_RULES = map(NAMES_RULES_LIB.getLastname());
    public static final RuleSet FIRST_NAME_RULES = map(NAMES_RULES_LIB.getFirstname());
    public static final RuleSet PATRONYMIC_NAME_RULES = map(NAMES_RULES_LIB.getMiddlename());
    public static final RuleSet REGULAR_TERM_RULES = map(REGULAR_RULES_LIB);

    private static RuleSet map(NameBean bean) {
        return new RuleSet(map(bean.getExceptions()), map(bean.getSuffixes()));
    }

    private static Rule[] map(Collection<RuleBean> beans) {
        return beans.stream().map(RuleLibrary::map).toArray(Rule[]::new);
    }

    private static Rule map(RuleBean bean) {
        return new Rule(map(bean.getGender()), bean.getTest().toArray(new String[0]), bean.getMods().toArray(new String[0]));
    }

    private static Gender map(String name) {
        switch (name) {
            case "female":
                return Gender.FEMALE;
            case "male":
                return Gender.MALE;
            case "androgynous":
                return Gender.NEUTER;
            default:
                throw new IllegalArgumentException("Wrong name: " + name);
        }
    }

    /**
     * Loads (from the class-path) a core library for inflecting Russian full-name (firstname patronymic surname, FPS).
     * It is assumed that the library would be updated from the main project on github,
     * custom changes should not be made to it.
     *
     * @return {@link RulesBean}
     * @see <a href='https://raw.githubusercontent.com/petrovich/petrovich-rules/master/rules.json'>rules.json</a>
     */
    private static RulesBean loadNamesBean() {
        return loadJsonBean(RulesBean.class, "/rules.json");
    }

    /**
     * Loads (from the class-path) a core library (rules-json) for inflecting Russian regular terms.
     * Initially, it was identical to the declension rules for surnames from {@link #loadNamesBean() rules.json}.
     *
     * @return {@link NameBean}
     */
    private static NameBean loadRegularBean() {
        return loadJsonBean(NameBean.class, "/regular.json");
    }

    private static <X> X loadJsonBean(Class<X> type, String file) {

        try (InputStream in = RuleLibrary.class.getResourceAsStream(file)) {
            return new ObjectMapper().readValue(in, type);
        } catch (IOException e) {
            throw new IllegalStateException("Can't load", e);
        }
    }

}

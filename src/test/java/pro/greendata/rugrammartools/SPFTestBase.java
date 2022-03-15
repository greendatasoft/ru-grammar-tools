package pro.greendata.rugrammartools;

/**
 * Created by @ssz on 12.03.2022.
 */
abstract class SPFTestBase {
    private final InflectionEngine engine = GrammarTools.getInflectionEngine();

    protected String inflectName(String name, SPF type, Case declension, Gender gender) {
        switch (type) {
            case LASTNAME:
                return engine.inflectSurname(name, declension, gender);
            case FIRSTNAME:
                return engine.inflectFirstname(name, declension, gender);
            case MIDDLENAME:
                return engine.inflectPatronymic(name, declension, gender);
            default:
                throw new AssertionError();
        }
    }

    public enum SPF {
        FIRSTNAME, MIDDLENAME, LASTNAME
    }
}

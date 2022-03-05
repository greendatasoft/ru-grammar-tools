package pro.greendata.rugrammartools.impl.beans;

/**
 * Created by @ssz on 04.12.2020.
 */
public class RulesBean {
    private NameBean lastname;
    private NameBean firstname;
    private NameBean middlename;

    public NameBean getLastname() {
        return lastname;
    }

    public void setLastname(NameBean lastname) {
        this.lastname = lastname;
    }

    public NameBean getFirstname() {
        return firstname;
    }

    public void setFirstname(NameBean firstname) {
        this.firstname = firstname;
    }

    public NameBean getMiddlename() {
        return middlename;
    }

    public void setMiddlename(NameBean middlename) {
        this.middlename = middlename;
    }

    @Override
    public String toString() {
        return String.format("RootBean{lastname=%s, firstname=%s, middlename=%s}", lastname, firstname, middlename);
    }
}

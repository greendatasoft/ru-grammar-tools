package pro.greendata.rugrammartools.impl.beans;

import java.util.List;

/**
 * Created by @ssz on 04.12.2020.
 */
@SuppressWarnings("unused")
public class NameBean {
    private List<RuleBean> exceptions;
    private List<RuleBean> suffixes;

    public List<RuleBean> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<RuleBean> exceptions) {
        this.exceptions = exceptions;
    }

    public List<RuleBean> getSuffixes() {
        return suffixes;
    }

    public void setSuffixes(List<RuleBean> suffixes) {
        this.suffixes = suffixes;
    }

    @Override
    public String toString() {
        return String.format("NameBean{exceptions=%s, suffixes=%s}", exceptions, suffixes);
    }
}

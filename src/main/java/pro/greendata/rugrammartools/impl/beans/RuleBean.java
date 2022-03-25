package pro.greendata.rugrammartools.impl.beans;

import java.util.List;

/**
 * Created by @ssz on 04.12.2020.
 */
@SuppressWarnings("unused")
public class RuleBean {
    private String gender;
    private String partOfSpeech;
    private String description;
    private Boolean plural;
    private Boolean animate;
    private List<String> mods;
    private List<String> test;
    private List<String> tags;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public List<String> getMods() {
        return mods;
    }

    public void setMods(List<String> mods) {
        this.mods = mods;
    }

    public List<String> getTest() {
        return test;
    }

    public void setTest(List<String> test) {
        this.test = test;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getPlural() {
        return plural;
    }

    public void setPlural(Boolean plural) {
        this.plural = plural;
    }

    public Boolean getAnimate() {
        return animate;
    }

    public void setAnimate(Boolean animate) {
        this.animate = animate;
    }

    @Override
    public String toString() {
        return String.format("RuleBean{gender='%s', mods=%s, test=%s}", gender, mods, test);
    }
}

package pro.greendata.rugrammartools.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pro.greendata.rugrammartools.impl.utils.RuleUtils;

public class RuleUtilsTest {

    @Test
    public void testChangeEnding() {
        Assertions.assertEquals("xxx", RuleUtils.changeEnding("xxx", "."));
        Assertions.assertEquals("xx", RuleUtils.changeEnding("xxx", "-"));
        Assertions.assertEquals("xyyy", RuleUtils.changeEnding("xxx", "--yyy"));
        Assertions.assertEquals("xxxzzz", RuleUtils.changeEnding("xxx", "zzz"));
    }
}

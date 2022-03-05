package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by @ssz on 18.02.2022.
 */
public class SpellingEngineTest {

    private final SpellingEngine spellingEngine = GrammarTools.getSpellingEngine();

    @Test
    public void testSmallIntegers() {
        Assertions.assertEquals("минус сорок два", spellingEngine.spell(-42));
        Assertions.assertEquals("сто двадцать три", spellingEngine.spell(123));
        Assertions.assertEquals("девятнадцать", spellingEngine.spell(19));
        Assertions.assertEquals("сто одиннадцать", spellingEngine.spell(111));
        Assertions.assertEquals("триста два", spellingEngine.spell(302));
        Assertions.assertEquals("пятьсот двадцать", spellingEngine.spell(520));
        Assertions.assertEquals("ноль", spellingEngine.spell(new BigDecimal(0)));
    }

    @Test
    public void testBigIntegers() {
        Assertions.assertEquals("двадцать пять тысяч", spellingEngine.spell(25_000));
        Assertions.assertEquals("двадцать шесть тысяч девятьсот девяносто девять", spellingEngine.spell(26_999));
        Assertions.assertEquals("сорок тысяч", spellingEngine.spell(40_000));
        Assertions.assertEquals("девятьсот тысяч", spellingEngine.spell(900_000));
        Assertions.assertEquals("один миллион двадцать четыре тысячи сто одиннадцать", spellingEngine.spell(1_024_111));
        Assertions.assertEquals("сто двадцать один миллион сто двадцать две тысячи четыреста пятьдесят шесть",
                spellingEngine.spell(121_122_456));
        Assertions.assertEquals("четыреста двадцать два миллиарда двадцать семь тысяч двести двадцать",
                spellingEngine.spell(422_000_027_220.));
        Assertions.assertEquals("один квадриллион три триллиона два миллиарда сорок две тысячи триста",
                spellingEngine.spell(new BigDecimal(new BigInteger("1003002000042300"))));
    }

    @Test
    public void testFractionNumbers() {
        Assertions.assertEquals("сорок две целых сорок две сотых", spellingEngine.spell(42.42));
        Assertions.assertEquals("минус сто сорок пять целых три десятых", spellingEngine.spell(-145.3));
        Assertions.assertEquals("одна целая три миллиона двести тридцать две тысячи триста пятьдесят шесть " +
                "десятимиллионных", spellingEngine.spell(1.3232356));
        Assertions.assertEquals("двадцать три целых девятьсот восемьдесят восемь тысяч восемьсот пятьдесят четыре " +
                "миллиардных", spellingEngine.spell(23.000988854));
        Assertions.assertEquals("ноль целых сорок четыре миллиарда четыреста одиннадцать миллионов сто тысяч сорок две " +
                "стомиллиардных", spellingEngine.spell(0.44411100042));
        Assertions.assertEquals("ноль целых одна десятитриллионная", spellingEngine.spell(0.000_000_000_000_1));
        Assertions.assertEquals("ноль целых девятьсот девяносто девять тысячных",
                spellingEngine.spell(new BigDecimal("0.999000000000000000000000000000000000000000000000000000000000000000")));
        Assertions.assertEquals("ноль", spellingEngine.spell(new BigDecimal("0.000")));
        Assertions.assertEquals("ноль целых сорок две квинтиллионных", spellingEngine.spell(0.000_000_000_000_000_042));
    }

    @Test
    public void testOverflow() {
        String tooBig = "999" + "0".repeat(63);
        Assertions.assertEquals("девятьсот девяносто девять вигинтиллионов", spellingEngine.spell(new BigDecimal(tooBig)));
        Assertions.assertEquals("девятьсот девяносто девять вигинтиллионов", spellingEngine.spell(new BigDecimal("0" + tooBig)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spellingEngine.spell(new BigDecimal(tooBig + "0")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spellingEngine.spell(new BigDecimal("1" + tooBig)));

        String tooSmall = "0." + "0".repeat(65) + "1";
        Assertions.assertEquals("ноль целых одна стовигинтиллионная", spellingEngine.spell(new BigDecimal("0." + tooSmall.substring(3))));
        Assertions.assertEquals("ноль целых одна стовигинтиллионная", spellingEngine.spell(new BigDecimal("00." + tooSmall.substring(3))));
        Assertions.assertEquals("ноль целых одна стовигинтиллионная", spellingEngine.spell(new BigDecimal("0." + tooSmall.substring(3) + "00")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spellingEngine.spell(new BigDecimal(tooSmall)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spellingEngine.spell(new BigDecimal(tooSmall + "0")));
        Assertions.assertThrows(IllegalArgumentException.class, () -> spellingEngine.spell(new BigDecimal("0" + tooSmall)));


        String withLongFractionPart = "42." + "0".repeat(60) + "123456789";
        Assertions.assertEquals("сорок две целых двенадцать тысяч триста сорок шесть стовигинтиллионных",
                spellingEngine.spell(new BigDecimal(withLongFractionPart)));
    }

    @Test
    public void testOrdinalFemale() {
        Assertions.assertEquals("нулевая", spellingEngine.spellOrdinal(0, Gender.FEMALE));
        Assertions.assertEquals("вторая", spellingEngine.spellOrdinal(2, Gender.FEMALE));
        Assertions.assertEquals("четвёртая", spellingEngine.spellOrdinal(4, Gender.FEMALE));
        Assertions.assertEquals("десятая", spellingEngine.spellOrdinal(10, Gender.FEMALE));
        Assertions.assertEquals("одиннадцатая", spellingEngine.spellOrdinal(11, Gender.FEMALE));
        Assertions.assertEquals("сорок две тысячи восемьсот первая", spellingEngine.spellOrdinal(42801, Gender.FEMALE));
        Assertions.assertEquals("один миллион сорокадвухтысячная", spellingEngine.spellOrdinal(1042000, Gender.FEMALE));
        Assertions.assertEquals("сорокадвухвигинтиллионная",
                spellingEngine.spellOrdinal(new BigInteger("42" + "0".repeat(63)), Gender.FEMALE));
    }

    @Test
    public void testOrdinalNeuter() {
        Assertions.assertEquals("нулевое", spellingEngine.spellOrdinal(0, Gender.NEUTER));
        Assertions.assertEquals("четвёртое", spellingEngine.spellOrdinal(4, Gender.NEUTER));
        Assertions.assertEquals("сорок второе", spellingEngine.spellOrdinal(42, Gender.NEUTER));
        Assertions.assertEquals("стосемидесятиодномиллиардное", spellingEngine.spellOrdinal(171000000000L, Gender.NEUTER));
        Assertions.assertEquals("сорок два миллиарда сорокадвухтысячное", spellingEngine.spellOrdinal(42000042000L, Gender.NEUTER));
    }

    @Test
    public void testOrdinalMale() {
        Assertions.assertEquals("нулевой", spellingEngine.spellOrdinal(0, Gender.MALE));
        Assertions.assertEquals("первый", spellingEngine.spellOrdinal(1, Gender.MALE));
        Assertions.assertEquals("девятый", spellingEngine.spellOrdinal(9, Gender.MALE));
        Assertions.assertEquals("пятнадцатый", spellingEngine.spellOrdinal(15, Gender.MALE));
        Assertions.assertEquals("девяностый", spellingEngine.spellOrdinal(90, Gender.MALE));
        Assertions.assertEquals("сотый", spellingEngine.spellOrdinal(100, Gender.MALE));
        Assertions.assertEquals("пятьсот сорок шестой", spellingEngine.spellOrdinal(546, Gender.MALE));
        Assertions.assertEquals("одна тысяча двести пятьдесят четвёртый", spellingEngine.spellOrdinal(1254, Gender.MALE));
        Assertions.assertEquals("сто сорок шесть тысяч двести восемьдесят шестой", spellingEngine.spellOrdinal(146286, Gender.MALE));
        Assertions.assertEquals("двухмиллионный", spellingEngine.spellOrdinal(2000000, Gender.MALE));
        Assertions.assertEquals("сто пятьдесят миллионов семьсот четвёртый", spellingEngine.spellOrdinal(150000704, Gender.MALE));
        Assertions.assertEquals("два миллиарда одна тысяча второй", spellingEngine.spellOrdinal(2000001002, Gender.MALE));
        Assertions.assertEquals("два миллиарда сто пятьдесят шесть миллионов девятьсот восемьдесят четыре тысячи триста пятьдесят седьмой",
                spellingEngine.spellOrdinal(2156984357L, Gender.MALE));
        Assertions.assertEquals("двухтысячный", spellingEngine.spellOrdinal(2000, Gender.MALE));
        Assertions.assertEquals("стотрёхтысячный", spellingEngine.spellOrdinal(103000, Gender.MALE));
        Assertions.assertEquals("двухсотоднотысячный", spellingEngine.spellOrdinal(201000, Gender.MALE));
        Assertions.assertEquals("сорокадвухтысячный", spellingEngine.spellOrdinal(42000, Gender.MALE));
        Assertions.assertEquals("сорокатысячный", spellingEngine.spellOrdinal(40000, Gender.MALE));
        Assertions.assertEquals("шестидесятитысячный", spellingEngine.spellOrdinal(60000, Gender.MALE));
    }

}
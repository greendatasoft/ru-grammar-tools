package com.gitlab.sszuev.inflector.impl;

import com.gitlab.sszuev.inflector.SpellingEngine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Created by @ssz on 18.02.2022.
 */
public class SpellingEngineImpl implements SpellingEngine {
    protected final MathContext context;
    protected final boolean stripTrailingZeros;
    protected final boolean trimFractionPart;

    protected static final List<String> HUNDREDS = List.of(
            "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот");
    protected static final List<String> TENS = List.of(
            "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто");
    protected static final List<String> NUMBERS_UP_TO_TWENTY = List.of(
            "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять",
            "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шеснадцать",
            "семнадцать", "восемнадцать", "девятнадцать");

    /**
     * {@code "Короткая шкала"}
     *
     * @see <a href='https://ru.wikipedia.org/wiki/%D0%98%D0%BC%D0%B5%D0%BD%D0%BD%D1%8B%D0%B5_%D0%BD%D0%B0%D0%B7%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F_%D1%81%D1%82%D0%B5%D0%BF%D0%B5%D0%BD%D0%B5%D0%B9_%D1%82%D1%8B%D1%81%D1%8F%D1%87%D0%B8'>Именные названия степеней тысячи</a>
     */
    protected static final List<String> BIGS = List.of(
            "тысяча", "миллион", "миллиард", "триллион", "квадриллион", "квинтиллион", "секстиллион", "септиллион",
            "октиллион", "нониллион", "дециллион", "ундециллион", "дуодециллион", "тредециллион", "кваттордециллион",
            "квиндециллион", "седециллион", "септдециллион", "октодециллион", "новемдециллион", "вигинтиллион");

    public SpellingEngineImpl() {
        this(MathContext.DECIMAL128, true, true);
    }

    public SpellingEngineImpl(MathContext context, boolean stripTrailingZeros, boolean trimFractionPart) {
        this.context = Objects.requireNonNull(context);
        this.stripTrailingZeros = stripTrailingZeros;
        this.trimFractionPart = trimFractionPart;
    }

    protected MathContext context() {
        return context;
    }

    @Override
    public String spell(BigDecimal number) {
        StringJoiner res = new StringJoiner(" ");
        int signum = number.signum();
        if (signum < 0) {
            res.add("минус");
        } else if (signum == 0) {
            return "ноль";
        }
        number = number.abs();
        if (stripTrailingZeros) {
            number = number.stripTrailingZeros();
        }
        List<Integer> integerTriples = NumberUtils.toTriples(number, context());
        if (integerTriples.size() > BIGS.size() + 1) {
            throw new IllegalArgumentException("The specified number is too big: " + number);
        }

        List<Integer> fractionTriples = NumberUtils.toTriples(NumberUtils.fraction(number, context()), context());
        if (number.scale() > BIGS.size() * 3 + 2) {
            if (!trimFractionPart || (integerTriples.size() == 1 && integerTriples.get(0) == 0)) { // no integer part
                throw new IllegalArgumentException("The specified number is too small: " + number);
            }
            number = number.setScale(BIGS.size() * 3 + 2, context().getRoundingMode());
            fractionTriples = NumberUtils.toTriples(NumberUtils.fraction(number, context()), context());
        }

        int[] last = printIntegerTriples(res, integerTriples, !fractionTriples.isEmpty());
        if (!fractionTriples.isEmpty()) {
            if (NumberUtils.isEmpty(last)) {
                res.add("ноль").add("целых");
            } else {
                res.add(last[2] == 1 ? "целая" : "целых");
            }
            int[] t = printIntegerTriples(res, fractionTriples, true);
            res.add(getFractionDigit(t, number.scale()));
        }
        return res.toString();
    }

    protected int[] printIntegerTriples(StringJoiner res, List<Integer> triples, boolean pluralEnding) {
        int[] t = null;
        for (int i = 0; i < triples.size(); i++) {
            t = NumberUtils.toTriple(triples.get(i));
            if (NumberUtils.isEmpty(t)) {
                continue;
            }
            int index = triples.size() - 2 - i;
            String s = tripleToString(t, index == 0 || pluralEnding);
            res.add(s);
            String big = getIntegerDigit(t, index);
            if (big != null) {
                res.add(big);
            }
        }
        return t;
    }

    protected String tripleToString(int[] t, boolean pluralEnding) {
        StringJoiner res = new StringJoiner(" ");
        if (t[0] != 0) {
            res.add(HUNDREDS.get(t[0] - 1));
        }
        if (t[1] != 0) {
            res.add(TENS.get(t[1] - 2));
        }
        if (t[2] != 0) {
            if (t[2] == 1 && pluralEnding) {
                res.add("одна");
            } else if (t[2] == 2 && pluralEnding) {
                res.add("две");
            } else {
                res.add(NUMBERS_UP_TO_TWENTY.get(t[2] - 1));
            }
        }
        return res.toString();
    }

    protected String getIntegerDigit(int[] t, int index) {
        if (index > 0) { // millions, trillions
            String big = BIGS.get(index);
            if (t[2] != 1) { // plural
                big += isTwoThreeFour(t[2]) ? "а" : "ов";
            }
            return big;
        } else if (index == 0) { // thousands
            if (t[2] == 1) { // singular
                return "тысяча";
            } else { // plural
                return isTwoThreeFour(t[2]) ? "тысячи" : "тысяч";
            }
        }
        return null;
    }

    protected String getFractionDigit(int[] t, int numberOfDigits) {
        int index = numberOfDigits / 3 - 1;
        int y = numberOfDigits % 3;
        String suffix = "";
        if (y == 1) {
            suffix = index < 0 ? "десятых" : "десяти";
        } else if (y == 2) {
            suffix = index < 0 ? "сотых" : "сто";
        }
        if (index == 0) {
            suffix += t[2] == 1 ? "тысячная" : "тысячных";
        } else if (index > 0) {
            suffix += BIGS.get(index);
            suffix += t[2] == 1 ? "ная" : "ных";
        }
        return suffix;
    }

    private boolean isTwoThreeFour(int t) {
        return t == 2 || t == 3 || t == 4;
    }
}

package pro.greendata.rugrammartools.impl;

import pro.greendata.rugrammartools.Gender;
import pro.greendata.rugrammartools.SpellingEngine;
import pro.greendata.rugrammartools.impl.utils.GrammarUtils;
import pro.greendata.rugrammartools.impl.utils.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    private static final List<String> NUMBER_BASES_FROM_ELEVEN_UP_TO_TWENTY = List.of(
            "одиннадцат", "двенадцат", "тринадцат", "четырнадцат", "пятнадцат", "шеснадцат",
            "семнадцат", "восемнадцат", "девятнадцат");

    protected static final List<String> NUMBERS_UP_TO_TWENTY = Stream.concat(Stream.of(
                    "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять"),
            NUMBER_BASES_FROM_ELEVEN_UP_TO_TWENTY.stream().map(x -> x + "ь")).collect(Collectors.toUnmodifiableList());

    private static final List<String> ORDINAL_HUNDRED_BASES = List.of(
            "сот", "двухсот", "трёхсот", "четырёхсот", "пятисот", "шестисот", "семисот", "восьмисот", "девятисот");
    protected static final List<String> ORDINAL_FEMALE_HUNDREDS = ORDINAL_HUNDRED_BASES.stream().map(x -> x + "ая")
            .collect(Collectors.toUnmodifiableList());
    protected static final List<String> ORDINAL_NEUTER_HUNDREDS = ORDINAL_HUNDRED_BASES.stream().map(x -> x + "ое")
            .collect(Collectors.toUnmodifiableList());
    protected static final List<String> ORDINAL_MALE_HUNDREDS = ORDINAL_HUNDRED_BASES.stream().map(x -> x + "ый")
            .collect(Collectors.toUnmodifiableList());

    protected static final List<String> ORDINAL_FEMALE_TENS = List.of(
            "двадцатая", "тридцатая", "сороковая", "пятидесятая", "шестидесятая", "семидесятая", "восьмидесятая", "девяностая");
    protected static final List<String> ORDINAL_NEUTER_TENS = List.of(
            "двадцатое", "тридцатое", "сороковое", "пятидесятое", "шестидесятое", "семидесятое", "восьмидесятое", "девяностое");
    protected static final List<String> ORDINAL_MALE_TENS = List.of(
            "двадцатый", "тридцатый", "сороковой", "пятидесятый", "шестидесятый", "семидесятый", "восьмидесятый", "девяностый");

    protected static final List<String> ORDINAL_FEMALE_NUMBERS_UP_TO_TWENTY = Stream.concat(Stream.of(
                    "первая", "вторая", "третья", "четвёртая", "пятая", "шестая", "седьмая", "восьмая", "девятая", "десятая"),
            NUMBER_BASES_FROM_ELEVEN_UP_TO_TWENTY.stream().map(x -> x + "ая")).collect(Collectors.toUnmodifiableList());
    protected static final List<String> ORDINAL_NEUTER_NUMBERS_UP_TO_TWENTY = Stream.concat(Stream.of(
                    "первое", "второе", "третье", "четвёртое", "пятое", "шестое", "седьмое", "восьмое", "девятое", "десятое"),
            NUMBER_BASES_FROM_ELEVEN_UP_TO_TWENTY.stream().map(x -> x + "ое")).collect(Collectors.toUnmodifiableList());
    protected static final List<String> ORDINAL_MALE_NUMBERS_UP_TO_TWENTY = Stream.concat(Stream.of(
                    "первый", "второй", "третий", "четвёртый", "пятый", "шестой", "седьмой", "восьмой", "девятый", "десятый"),
            NUMBER_BASES_FROM_ELEVEN_UP_TO_TWENTY.stream().map(x -> x + "ый")).collect(Collectors.toUnmodifiableList());

    protected static final List<String> ORDINAL_HUNDRED_PREFIXES = List.of( // двухсотмилиооная, девятисоттысячный
            "сто", "двухсот", "трёхсот", "четырёхсот", "пятисот", "шестисот", "семисот", "восемсот", "девятисот");
    protected static final List<String> ORDINAL_TEN_PREFIXES = List.of( // сорокамиллионный, тридцатипятитысячный
            "двадцати", "тридцати", "сорока", "пятидесяти", "шестидесяти", "семидесяти", "восьмидесяти", "девяносто");
    protected static final List<String> ORDINAL_NUMBER_UP_TO_TWENTY_PREFIXES = Stream.concat(Stream.of(
                    "одно", "двух", "трёх", "четырёх", "пяти", "шести", "семи", "восьми", "девяти", "десяти"),
            NUMBER_BASES_FROM_ELEVEN_UP_TO_TWENTY.stream().map(x -> x + "и")).collect(Collectors.toUnmodifiableList());

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
                if (integerTriples.size() == 1) {
                    res.add("ноль");
                }
                res.add("целых");
            } else {
                res.add(last[2] == 1 ? "целая" : "целых");
            }
            int[] t = printIntegerTriples(res, fractionTriples, true);
            res.add(getFractionDigit(t, number.scale()));
        }
        return res.toString();
    }

    /**
     * A couple of rules:
     * {@code Сложные порядковые числительные как производные от количественных пишутся слитно, а составные раздельно:
     * шестнадцатый, семидесятый, девятисотый; тысяча девятьсот девяносто первый, три тысячи восьмой}
     * {@code Порядковые числительные, оканчивающиеся на тысячный, миллионный, миллиардный, пишутся слитно:
     * стотридцатитысячный (сто тридцать тысяч), пятидесятичетырехмиллионный (пятьдесят четыре миллиона),
     * двухсоттридцатимиллиардный (двести тридцать миллиардов)}
     *
     * @param number {@link BigInteger}, not {@code null}
     * @param gender {@link Gender}
     * @return {@code String}
     * @see <a href='https://videotutor-rusyaz.ru/uchenikam/teoriya/146-pravopisanieimenchislitelnyh.html'>Правописание имен числительных</a>
     * @see <a href='http://new.gramota.ru/spravka/buro/search-answer?s=%D1%82%D1%8B%D1%81%D1%8F%D1%87%D0%BD%D1%8B%D0%B9'>тысячный? однатысячный? однотысячный?</a>
     * @see <a href='http://new.gramota.ru/spravka/buro/search-answer/?s=309317'>gramota.ru</a>
     */
    @Override
    public String spellOrdinal(BigInteger number, Gender gender) {
        if (number.signum() < 0) {
            throw new IllegalArgumentException("Negative input");
        }
        if (BigInteger.ZERO.equals(number)) {
            return GrammarUtils.select("нулевая", "нулевое", "нулевой", gender);
        }

        List<Integer> triples = NumberUtils.toTriples(number);
        StringJoiner res = new StringJoiner(" ");
        int lastIndex = triples.size() - 1;
        if (triples.get(lastIndex) != 0) { // раздельно: тысяча девятьсот девяносто первый, сорок второй, четрехсотый
            printIntegerTriples(res, triples, false, lastIndex);
            int[] t = NumberUtils.toTriple(triples.get(lastIndex));
            res.add(firstOrdinalTripleToString(t, gender));
            return res.toString();
        }
        lastIndex = IntStream.range(0, triples.size()).map(i -> triples.size() - i - 1)
                .filter(x -> triples.get(x) != 0).findFirst().orElseThrow();
        printIntegerTriples(res, triples, false, lastIndex);
        // слитное написание: сорокадвухтысячный, пятидесятичетырехмиллионный
        int[] t = NumberUtils.toTriple(triples.get(lastIndex));
        int rank = triples.size() - lastIndex - 1;
        res.add(lastOrdinalTripleToString(t, gender, rank));
        return res.toString();
    }

    protected int[] printIntegerTriples(StringJoiner res, List<Integer> triples, boolean hasFractionPart) {
        return printIntegerTriples(res, triples, hasFractionPart, triples.size());
    }

    protected int[] printIntegerTriples(StringJoiner res, List<Integer> triples, boolean hasFractionPart, int maxExclusive) {
        int[] t = null;
        for (int i = 0; i < maxExclusive; i++) {
            t = NumberUtils.toTriple(triples.get(i));
            if (NumberUtils.isEmpty(t)) {
                continue;
            }
            int bigIndex = triples.size() - 2 - i;
            String s = tripleToString(t, bigIndex == 0 || (bigIndex < 0 && hasFractionPart));
            res.add(s);
            String big = getIntegerDigit(t, bigIndex);
            if (big != null) {
                res.add(big);
            }
        }
        return t;
    }

    protected String tripleToString(int[] t, boolean isFractionOrThousand) {
        StringJoiner res = new StringJoiner(" ");
        if (t[0] != 0) {
            res.add(HUNDREDS.get(t[0] - 1));
        }
        if (t[1] != 0) {
            res.add(TENS.get(t[1] - 2));
        }
        if (t[2] != 0) {
            if (t[2] == 1 && isFractionOrThousand) { // одна тысяча, одна целая одна десятая
                res.add("одна");
            } else if (t[2] == 2 && isFractionOrThousand) { // две тысячи, две целых две десятых
                res.add("две");
            } else { // один миллиард, два миллиона
                res.add(NUMBERS_UP_TO_TWENTY.get(t[2] - 1));
            }
        }
        return res.toString();
    }

    protected String firstOrdinalTripleToString(int[] t, Gender g) {
        StringJoiner res = new StringJoiner(" ");
        if (t[0] != 0) {
            int index = t[0] - 1;
            if (t[1] == 0 && t[2] == 0) {
                res.add(GrammarUtils.select(ORDINAL_FEMALE_HUNDREDS, ORDINAL_NEUTER_HUNDREDS, ORDINAL_MALE_HUNDREDS, g).get(index));
                return res.toString();
            }
            res.add(HUNDREDS.get(index));
        }
        if (t[1] != 0) {
            int index = t[1] - 2;
            if (t[2] == 0) {
                res.add(GrammarUtils.select(ORDINAL_FEMALE_TENS, ORDINAL_NEUTER_TENS, ORDINAL_MALE_TENS, g).get(index));
                return res.toString();
            }
            res.add(TENS.get(index));
        }
        res.add(GrammarUtils.select(ORDINAL_FEMALE_NUMBERS_UP_TO_TWENTY, ORDINAL_NEUTER_NUMBERS_UP_TO_TWENTY,
                ORDINAL_MALE_NUMBERS_UP_TO_TWENTY, g).get(t[2] - 1));
        return res.toString();
    }

    protected String lastOrdinalTripleToString(int[] t, Gender g, int rank) {
        StringJoiner res = new StringJoiner("");
        if (t[0] != 0) {
            int index = t[0] - 1;
            res.add(ORDINAL_HUNDRED_PREFIXES.get(index));
        }
        if (t[1] != 0) {
            int index = t[1] - 2;
            res.add(ORDINAL_TEN_PREFIXES.get(index));
        }
        if (t[2] != 0) {
            res.add(ORDINAL_NUMBER_UP_TO_TWENTY_PREFIXES.get(t[2] - 1));
        }
        res.add(selectRank(g, rank));
        return res.toString();
    }

    private String selectRank(Gender g, int rank) {
        if (rank < 1) {
            throw new IllegalArgumentException("Wrong rang: " + rank);
        }
        String res;
        if (rank == 1) {
            res = "тысяч";
        } else {
            res = BIGS.get(rank - 1);
        }
        String ending = GrammarUtils.select("ная", "ное", "ный", g);
        return res + ending;
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
            suffix = index < 0 ? (t[2] == 1 ? "десятая" : "десятых") : "десяти";
        } else if (y == 2) {
            suffix = index < 0 ? (t[2] == 1 ? "сотая" : "сотых") : "сто";
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

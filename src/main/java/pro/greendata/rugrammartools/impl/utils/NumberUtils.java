package pro.greendata.rugrammartools.impl.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by @ssz on 18.02.2022.
 */
public class NumberUtils {
    static final BigDecimal BIG_DECIMAL_THOUSAND = BigDecimal.valueOf(1000);
    static final BigInteger BIG_INTEGER_THOUSAND = BigInteger.valueOf(1000);

    public static List<Integer> toTriples(BigDecimal n, MathContext context) {
        if (isZero(n)) {
            return List.of();
        }
        List<Integer> res = new ArrayList<>();
        do {
            BigDecimal[] array = n.divideAndRemainder(BIG_DECIMAL_THOUSAND, context);
            res.add(0, array[1].intValue());
            n = array[0];
        } while (!isZero(n));
        return res;
    }

    public static List<Integer> toTriples(BigInteger n) {
        if (BigInteger.ZERO.equals(n)) {
            return List.of();
        }
        List<Integer> res = new ArrayList<>();
        do {
            BigInteger[] array = n.divideAndRemainder(BIG_INTEGER_THOUSAND);
            res.add(0, array[1].intValue());
            n = array[0];
        } while (!BigInteger.ZERO.equals(n));
        return res;
    }

    public static BigDecimal fraction(BigDecimal n, MathContext context) {
        if (isZero(n)) {
            return BigDecimal.ZERO;
        }
        int scale = n.scale();
        return n.remainder(BigDecimal.ONE, context).movePointRight(scale);
    }

    public static boolean isZero(BigDecimal n) {
        //noinspection NumberEquality
        return BigDecimal.ZERO == n || BigDecimal.ZERO.compareTo(n) == 0;
    }

    public static int[] toTriple(int n) {
        int[] res = new int[3];
        res[0] = n / 100;
        n = n % 100;
        if (n < 20) {
            res[2] = n;
            return res;
        }
        res[1] = n / 10;
        res[2] = n % 10;
        return res;
    }

    public static boolean isEmpty(int[] triple) {
        return triple[2] == 0 && triple[1] == 0 && triple[0] == 0;
    }

}

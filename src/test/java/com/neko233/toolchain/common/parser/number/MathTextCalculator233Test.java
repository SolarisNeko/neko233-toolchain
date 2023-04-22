package com.neko233.toolchain.common.parser.number;

import com.neko233.toolchain.common.base.MapUtils233;
import com.neko233.toolchain.parser.number.MathTextCalculator233;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author SolarisNeko
 * Date on 2023-01-29
 */
public class MathTextCalculator233Test {


    @Test
    public void test_math_by_template() throws Exception {
        final String mathTemplate = " ${attack} * 3 * ${userLevel} ";

        Map<String, Object> kvMap = MapUtils233.of(
                "attack", 3,
                "userLevel", 10
        );

        BigDecimal bigDecimal = MathTextCalculator233.executeExpressionByTemplate(mathTemplate, kvMap);
        assertEquals("90.00", bigDecimal.toString());
    }

    @Test
    public void t1_multi() throws Exception {
        String mathStr = "3 * 2 + 1";
        BigDecimal bigDecimal = MathTextCalculator233.executeExpression(mathStr);
        assertEquals("7.00", bigDecimal.toString());
    }

    @Test
    public void t2_divide() throws Exception {
        String mathStr = "3 / 2 + 1";
        BigDecimal bigDecimal = MathTextCalculator233.executeExpression(mathStr);
        assertEquals("2.50", bigDecimal.toString());
    }


    @Test
    public void t3_multi_calculate() throws Exception {
        // 10
        String mathStr = "2 * 5 - 5 / 5 + 2 - 2/4 - 2/4 ";
        BigDecimal bigDecimal = MathTextCalculator233.executeExpression(mathStr);
        assertEquals("10.00", bigDecimal.toString());
    }

    @Test
    public void t4_big_number() throws Exception {
        String mathStr = "10000 * 10000 + 10000 * 10000 * 5 ";
        BigDecimal bigDecimal = MathTextCalculator233.executeExpression(mathStr);
        assertEquals("600000000.00", bigDecimal.toString());
    }

    @Test
    public void t5_power() throws Exception {
        String mathStr = "2 ^ 2  ";
        BigDecimal bigDecimal = MathTextCalculator233.executeExpression(mathStr);
        assertEquals("4.00", bigDecimal.toString());
    }

    /**
     * 开 3 次方根号
     */
    @Test
    public void t6_sqrt_n_count() throws Exception {
        String mathStr = "3/-81  ";
        BigDecimal bigDecimal = MathTextCalculator233.executeExpression(mathStr);
        assertEquals("3.00", bigDecimal.toString());
    }

}
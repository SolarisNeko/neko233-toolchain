package com.neko233.toolchain.explainer.functionText;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FunctionText233ParserTest {

    @Test
    public void test_base() {
        FunctionText233 parse = FunctionTextParser233.parse("halo()");
        Assert.assertEquals("halo", parse.getFunctionName());
    }

    @Test
    public void test_simpleFormat() {
        FunctionText233 parse = FunctionTextParser233.parse("halo");

        Assert.assertEquals("halo", parse.getFunctionName());
    }

    @Test
    public void test_functionTextBatchParse() {
        List<FunctionText233> functionText233s = FunctionTextParser233.parseBatch("halo1 && halo2( ) ");

        Assert.assertEquals(2, functionText233s.size());
        Assert.assertEquals("halo1", functionText233s.get(0).getFunctionName());
        Assert.assertEquals("halo2", functionText233s.get(1).getFunctionName());
    }

}
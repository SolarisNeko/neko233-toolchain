package com.neko233.common.reflect;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtilsTest {

    public int demo() {
        return 0;
    }

    @Test
    public void test1() throws InvocationTargetException, IllegalAccessException {
        Method demo = ReflectUtils.getMethodByName(ReflectUtilsTest.class, "demo");
        Object invoke = demo.invoke(this);
        Assert.assertEquals(0, invoke);
    }


}
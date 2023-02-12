package com.neko233.toolchain.validator.impl.my;

import com.neko233.toolchain.validator.ValidateContext;
import com.neko233.toolchain.validator.Validator233;
import com.neko233.toolchain.validator.annotation.Number;
import com.neko233.toolchain.validator.impl.NumberValidator;
import org.junit.Assert;
import org.junit.Test;

public class NumberValidator233Test {

    class Demo {
        @Number(min = 1, max = 99)
        public int count;
    }

    @Test
    public void testTrue() {
        Validator233.registerByScan(NumberValidator.class.getPackage().getName());
        Demo demo = new Demo();
        demo.count = 86;

        ValidateContext validate = Validator233.validate(demo);
        Assert.assertTrue(validate.isOk());
    }

    @Test
    public void testFailure() {
        Validator233.registerByScan(NumberValidator.class.getPackage().getName());
        Demo demo = new Demo();
        demo.count = 101;

        ValidateContext validate = Validator233.validate(demo);
        Assert.assertFalse(validate.isOk());
    }
}
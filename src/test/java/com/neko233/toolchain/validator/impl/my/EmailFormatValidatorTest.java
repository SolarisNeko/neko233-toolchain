package com.neko233.toolchain.validator.impl.my;

import com.neko233.toolchain.validator.ValidateContext;
import com.neko233.toolchain.validator.Validator233;
import com.neko233.toolchain.validator.annotation.EmailFormat;
import org.junit.Assert;
import org.junit.Test;

public class EmailFormatValidatorTest {

    @Test
    public void validateOk() {
        Demo demo = new Demo();
        demo.email = "12345@qq.com";

        ValidateContext validate = Validator233.validate(demo);
        Assert.assertTrue(validate.isOk());
    }

    public static class Demo {
        @EmailFormat(emailSuffix = "@qq.com", emailPrefixLength = 5)
        String email;
    }
}
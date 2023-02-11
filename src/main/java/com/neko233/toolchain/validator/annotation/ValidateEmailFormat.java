package com.neko233.toolchain.validator.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateEmailFormat {

    String tips() default "";

    /**
     * 邮箱, 前缀长度
     */
    int emailPrefixLength() default 20;

    /**
     * 邮箱后缀
     */
    String[] emailSuffix() default {"@neko233.com"};



}


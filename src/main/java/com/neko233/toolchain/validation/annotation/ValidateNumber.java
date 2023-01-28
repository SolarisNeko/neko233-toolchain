package com.neko233.toolchain.validation.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateNumber {

    String tips() default "";

    /**
     * 最小值
     */
    int min();

    /**
     * 最大值
     */
    int max();

}

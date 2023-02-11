package com.neko233.toolchain.common.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadNotSafe {

    String tips() default "";

}

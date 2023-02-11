package com.neko233.toolchain.validator;

import java.lang.annotation.Annotation;

public interface ValidateApi<ANNOTATION extends Annotation, FIELD_VALUE> {

    /**
     * @return 注解类型
     */
    Class<? extends ANNOTATION> getAnnotationType();

    /**
     * is OK ?
     *
     * @param annotation 注解
     * @param fieldValue 字段的值
     * @return 根据 annotation 返回是否正确
     */
    boolean validateOk(ANNOTATION annotation, FIELD_VALUE fieldValue);

    /**
     * 渲染原因模板
     *
     * @param annotation 注解
     * @param fieldName
     * @param fieldValue 字段值
     * @return 原因
     */
    String getReason(ANNOTATION annotation, String fieldName, FIELD_VALUE fieldValue);

}

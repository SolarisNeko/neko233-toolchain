package com.neko233.toolchain.validator.impl.my;

import com.neko233.toolchain.common.base.StringUtils233;
import com.neko233.toolchain.validator.ValidateApi;
import com.neko233.toolchain.validator.annotation.NotNull;

/**
 * NotNull Validator
 */
public class NotNullValidator implements ValidateApi<NotNull, Object> {

    @Override
    public Class<? extends NotNull> getAnnotationType() {
        return NotNull.class;
    }

    @Override
    public boolean validateOk(NotNull annotation, Object fieldValue) {
        if (fieldValue == null) {
            return false;
        }
        return true;
    }

    @Override
    public String getReason(NotNull annotation, String fieldName, Object fieldValue) {
        if (StringUtils233.isNotBlank(annotation.tips())) {
            return annotation.tips();
        }
        return StringUtils233.format("field = {}. value is null", fieldName, annotation.tips());
    }

}

package com.neko233.toolchain.validator.impl.my;

import com.neko233.toolchain.common.base.StringUtils233;
import com.neko233.toolchain.validator.ValidateApi;
import com.neko233.toolchain.validator.annotation.ValidateNumber;

/**
 * ValidateNumber Validator
 */
public class NumberValidator implements ValidateApi<ValidateNumber, Number> {

    @Override
    public Class<? extends ValidateNumber> getAnnotationType() {
        return ValidateNumber.class;
    }

    @Override
    public boolean validateOk(ValidateNumber annotation, Number fieldValue) {
        int min = annotation.min();
        int max = annotation.max();

        if (min == 0 && max == 0) {
            return true;
        }

        int value = fieldValue.intValue();
        if (value < min) {
            return false;
        }
        if (value > max) {
            return false;
        }
        return true;
    }

    @Override
    public String getReason(ValidateNumber annotation, String fieldName, Number fieldValue) {
        if (StringUtils233.isNotBlank(annotation.tips())) {
            return annotation.tips();
        }
        return String.format("field = %s, Your number is not in constraint. min = %s, max = %s, your value = %s",
                fieldName, annotation.min(), annotation.max(), fieldValue);
    }

}

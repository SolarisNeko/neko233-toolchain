package com.neko233.toolchain.validator.impl.my;

import com.neko233.toolchain.common.base.StringUtils233;
import com.neko233.toolchain.validator.ValidateApi;
import com.neko233.toolchain.validator.annotation.ValidateEmailFormat;

/**
 * ValidateEmailFormat Validator
 */
public class ValidateEmailFormatValidator implements ValidateApi<ValidateEmailFormat, Object> {

    @Override
    public Class<? extends ValidateEmailFormat> getAnnotationType() {
        return ValidateEmailFormat.class;
    }

    @Override
    public boolean validateOk(ValidateEmailFormat annotation, Object fieldValue) {
        String emailString = String.valueOf(fieldValue);

        String[] emailSuffixList = annotation.emailSuffix();

        String mySuffix = "";
        for (String suffix : emailSuffixList) {
            if (emailString.contains(suffix)) {
                mySuffix = suffix;
            }
        }
        if (StringUtils233.isBlank(mySuffix)) {
            return false;
        }

        String[] split = emailString.split(mySuffix);
        if (split.length != 1) {
            return false;
        }

        String prefix = split[0];
        if (prefix.length() > annotation.emailPrefixLength()) {
            return false;
        }
        return true;
    }

    @Override
    public String getReason(ValidateEmailFormat annotation, String fieldName, Object fieldValue) {
        return StringUtils233.format("tips = {}. field = {}. value is {}", annotation.tips(), fieldName, fieldValue);
    }

}

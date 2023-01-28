package com.neko233.toolchain.validation.impl.jakarta;

import com.neko233.toolchain.common.base.StringUtils233;
import com.neko233.toolchain.validation.ValidateApi;
import jakarta.validation.constraints.Email;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Email Validator
 */
public class JakartaEmailValidator implements ValidateApi<Email, String> {

    @Override
    public Class<? extends Email> getAnnotationType() {
        return Email.class;
    }

    @Override
    public boolean handle(Email value, String fieldValue) {
        // 大小写敏感
        Pattern pattern = Pattern.compile(value.regexp(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fieldValue);
        return matcher.matches();
    }

    @Override
    public String getReason(Email email, String fieldValue) {
        if (StringUtils233.isNotBlank(email.message())) {
            return email.message();
        }
        return String.format("%s is not target email format. your email format = %s", fieldValue, email.regexp());
    }

}
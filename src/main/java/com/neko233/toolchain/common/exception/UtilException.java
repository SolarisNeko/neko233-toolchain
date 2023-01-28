package com.neko233.toolchain.common.exception;

import com.neko233.toolchain.common.base.ExceptionUtils233;
import com.neko233.toolchain.common.base.StringUtils233;

/**
 * 工具类异常
 */
public class UtilException extends RuntimeException {

    private static final long serialVersionUID = 54312L;

    public UtilException(Throwable e) {
        super(ExceptionUtils233.getMessage(e), e);
    }

    public UtilException(String message) {
        super(message);
    }

    public UtilException(String messageTemplate, Object... params) {
        super(StringUtils233.format(messageTemplate, params));
    }

    public UtilException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public UtilException(String message, Throwable throwable, boolean enableSuppression, boolean writableStackTrace) {
        super(message, throwable, enableSuppression, writableStackTrace);
    }

    public UtilException(Throwable throwable, String messageTemplate, Object... params) {
        super(StringUtils233.format(messageTemplate, params), throwable);
    }

}

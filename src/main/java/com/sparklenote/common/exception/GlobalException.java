package com.sparklenote.common.exception;

import com.sparklenote.common.error.code.BaseErrorCode;

public class GlobalException extends RuntimeException {

    private BaseErrorCode errorCode;

    public GlobalException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}

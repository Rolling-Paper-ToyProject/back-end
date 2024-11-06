package com.sparklenote.common.exception;

import com.sparklenote.common.error.code.BaseErrorCode;

public class StudentException extends RuntimeException {

    private BaseErrorCode errorCode;

    public StudentException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}

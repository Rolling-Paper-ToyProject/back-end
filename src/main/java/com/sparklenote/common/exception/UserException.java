package com.sparklenote.common.exception;

import com.sparklenote.common.error.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {

    private BaseErrorCode errorCode;

    public UserException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}

package com.sparklenote.common.exception;

import com.sparklenote.common.error.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class RollException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public RollException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}

package com.sparklenote.common.exception;

import com.sparklenote.common.error.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class PaperException extends RuntimeException {

    private BaseErrorCode errorCode;

    public PaperException(BaseErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}

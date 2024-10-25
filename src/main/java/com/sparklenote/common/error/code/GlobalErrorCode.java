package com.sparklenote.common.error.code;

import com.sparklenote.common.error.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements BaseErrorCode {

    INTERNAL_SERVER_ERROR(500, "서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    GlobalErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }


    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(errorCode, errorMessage);
    }
}

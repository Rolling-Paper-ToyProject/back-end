package com.sparklenote.common.error.code;

import com.sparklenote.common.error.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements BaseErrorCode{

    TOKEN_IS_NOT_VALID(401, "Refresh Token이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    UserErrorCode(int errorCode, String errorMessage, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return null;
    }
}

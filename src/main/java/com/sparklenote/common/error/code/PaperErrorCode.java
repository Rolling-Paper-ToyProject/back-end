package com.sparklenote.common.error.code;

import com.sparklenote.common.error.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PaperErrorCode implements BaseErrorCode{
    PAPER_NOT_FOUND(404, "Paper를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAPER_DELETE_FORBIDDEN(403, "Paper 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final int errorCode;
    private final String errorMessage;
    private final HttpStatus status;

    PaperErrorCode(int errorCode, String message, HttpStatus status) {
        this.errorCode = errorCode;
        this.errorMessage = message;
        this.status = status;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return new ErrorResponse(this.errorCode, this.errorMessage);
    }
}


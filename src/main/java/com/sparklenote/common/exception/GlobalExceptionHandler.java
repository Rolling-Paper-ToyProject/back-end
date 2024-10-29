package com.sparklenote.common.exception;

import com.sparklenote.common.error.code.BaseErrorCode;
import com.sparklenote.common.error.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.sparklenote.common.error.code.GlobalErrorCode.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // RuntimeException 만 잡아서 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRollException(RuntimeException ex) {
        log.error(">>>>> Internal Server Error : {}", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR.getStatus())
                .body(INTERNAL_SERVER_ERROR.getErrorResponse());
    }

    @ExceptionHandler(RollException.class)
    public ResponseEntity<ErrorResponse> handleRollException(RollException ex) {
        log.error(">>>>> RollException : {}", ex);
        BaseErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getErrorResponse());
    }

    @ExceptionHandler(PaperException.class)
    public ResponseEntity<ErrorResponse> handlePaperException(PaperException ex) {
        log.error(">>>>> PaperException : {}", ex);
        BaseErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getErrorResponse());
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex) {
        log.error(">>>>> UserException : {}", ex);
        BaseErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(errorCode.getErrorResponse());
    }
}

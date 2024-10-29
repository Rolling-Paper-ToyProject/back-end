package com.sparklenote.common.error.code;

import com.sparklenote.common.error.response.ErrorResponse;
import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    int getErrorCode();

    String getErrorMessage();

    HttpStatus getStatus();

    ErrorResponse getErrorResponse();
}

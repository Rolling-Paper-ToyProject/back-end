package com.sparklenote.common.error.code;

import com.sparklenote.common.error.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RollErrorCode implements BaseErrorCode {

	ROLL_NOT_FOUND(404, "Roll을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);


	private final int errorCode;
	private final String errorMessage;
	private final HttpStatus status;

	RollErrorCode(int errorCode, String message, HttpStatus status) {
		this.errorCode = errorCode;
		this.errorMessage = message;
		this.status = status;
	}

	@Override
	public ErrorResponse getErrorResponse() {
		return new ErrorResponse(this.errorCode, this.errorMessage);
	}
}

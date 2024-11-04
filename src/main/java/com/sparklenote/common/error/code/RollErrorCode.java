package com.sparklenote.common.error.code;

import com.sparklenote.common.error.response.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum RollErrorCode implements BaseErrorCode {

	VALIDATION_FAILED(400, "유효성 검사에 실패했습니다", HttpStatus.BAD_REQUEST),
	ROLL_NOT_FOUND(404, "Roll을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	UNAUTHORIZED_STUDENT(403, "Student는 Roll을 생성할 수 없습니다.", HttpStatus.UNAUTHORIZED),
	ROLL_NAME_NOT_CHANGED(400, "Roll 이름이 변경되지 않았습니다.", HttpStatus.BAD_REQUEST),
	INVALID_CLASS_CODE(400, "학급코드가 일치하지 않습니다", HttpStatus.BAD_REQUEST);


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

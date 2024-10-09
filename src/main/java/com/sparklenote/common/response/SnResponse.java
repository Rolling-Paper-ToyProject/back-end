package com.sparklenote.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparklenote.common.code.GlobalSuccessCode;
import lombok.Getter;

import static com.sparklenote.common.code.GlobalSuccessCode.SUCCESS;

@Getter
public class SnResponse<T> {
	private int code;
	private String message;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T data;

	public SnResponse(T data) {
		this.code = SUCCESS.getCode();
		this.message = SUCCESS.getMessage();
		this.data = data;
	}

	public SnResponse(GlobalSuccessCode statusCode, T data) {
		this.code = statusCode.getCode();
		this.message = statusCode.getMessage();
		this.data = data;
	}
}

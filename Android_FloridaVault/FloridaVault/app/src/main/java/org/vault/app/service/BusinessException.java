package org.vault.app.service;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private String errorCode;
	private String data;

	public BusinessException(String errorCode) {
		super("Error code: " + errorCode);
		this.errorCode = errorCode;
	}

	public BusinessException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public String getErrorCode() {
		return errorCode;
	}
}

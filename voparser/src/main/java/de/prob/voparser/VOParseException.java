package de.prob.voparser;

public class VOParseException extends Exception {

	public enum ErrorType {
		PARSING, SCOPING, TYPECHECKING
	}

	private ErrorType errorType;

	public VOParseException(String message, ErrorType errorType) {
		super(message);
		this.errorType = errorType;
	}

	public ErrorType getErrorType() {
		return errorType;
	}
}

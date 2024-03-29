package de.be4.ltl.core.parser;

import de.be4.ltl.core.parser.internal.UniversalToken;

@SuppressWarnings("serial")
public class LtlParseException extends Exception {

	private final UniversalToken token;

	/**
	 * This constructor is not intended to be referenced by clients.
	 * 
	 * @param token
	 *            the token where the error occurred
	 * @param msg
	 *            the error message
	 */
	public LtlParseException(final UniversalToken token, final String msg) {
		super(msg);
		this.token = token;
	}

	public LtlParseException(final UniversalToken token, final Throwable cause) {
		super(cause);
		this.token = token;
	}

	public LtlParseException(final UniversalToken token, final String message, final Throwable cause) {
		super(message, cause);
		this.token = token;
	}

	public String getTokenString() {
		return token == null ? null : token.getText();
	}

	public int getTokenLine() {
		return token == null ? 0 : token.getLine();
	}

	public int getTokenColumn() {
		return token == null ? 0 : token.getColumn();
	}

}

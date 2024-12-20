package de.be4.classicalb.core.parser.exceptions;

import de.be4.classicalb.core.parser.node.Token;

@SuppressWarnings("serial")
public class BParseException extends RuntimeException {
	private final Token token;
	private final String realMsg;

	/**
	 * @param token the token at which the parse error occurred
	 * @param message parse error message, possibly including SableCC line/column info at the start
	 * @deprecated Use {@link #BParseException(Token, String, String)} instead,
	 *     which doesn't attempt to remove SableCC line/column info from the message.
	 */
	@Deprecated
	public BParseException(final Token token, final String message) {
		this(token, message, message.substring(message.indexOf(']') + 1));
	}

	public BParseException(Token token, String msg, String realMsg) {
		super(msg);
		this.token = token;
		this.realMsg = realMsg;

	}

	public BParseException(Token token, String msg, String realMsg, Throwable throwable) {
		super(msg, throwable);
		this.token = token;
		this.realMsg = realMsg;

	}

	/**
	 * {@link Token} which caused the parse exception. May be <code>null</code>
	 * if no special token was affected.
	 * 
	 * @return the token which caused the parse exception
	 */
	public Token getToken() {
		return token;
	}


	public String getRealMsg() {
		return realMsg;
	}
}

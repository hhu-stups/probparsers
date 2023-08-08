package de.be4.eventb.core.parser;

import de.be4.eventb.core.parser.node.Token;

@SuppressWarnings("serial")
public class EventBParseException extends RuntimeException {
	private final Token token;

	public EventBParseException(final Token token, final String message) {
		super(message);
		this.token = token;
	}

	/**
	 * {@link Token} which caused the parse exception. May be <code>null</code>
	 * if no special token was affected.
	 * 
	 * @return the token which caused the parse exception. May be <code>null</code>.
	 */
	public Token getToken() {
		return token;
	}
}

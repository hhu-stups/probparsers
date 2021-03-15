package de.be4.classicalb.core.parser.exceptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.preparser.node.Token;

@SuppressWarnings("serial")
public class PreParseException extends Exception {

	private final List<Token> tokens;

	public PreParseException(final List<Token> tokens, final String message, final Throwable cause) {
		super(message, cause);
		this.tokens = tokens;
	}

	/**
	 * @deprecated Use {@link #PreParseException(List, String, Throwable)} with a {@link List} argument instead.
	 */
	@Deprecated
	public PreParseException(final Token[] tokens, final String message, final Throwable cause) {
		this(Arrays.asList(tokens), message, cause);
	}

	public PreParseException(final Token token, final String message, final Throwable cause) {
		this(Collections.singletonList(token), message, cause);
	}

	public PreParseException(final String message, final Throwable cause) {
		this(Collections.emptyList(), message, cause);
	}

	public PreParseException(final List<Token> tokens, final String message) {
		super(message);
		this.tokens = tokens;
	}

	/**
	 * @deprecated Use {@link #PreParseException(List, String)} with a {@link List} argument instead.
	 */
	@Deprecated
	public PreParseException(final Token[] tokens, final String message) {
		this(Arrays.asList(tokens), message);
	}

	public PreParseException(final Token token, final String message) {
		this(Collections.singletonList(token), message);
	}

	public PreParseException(final String message) {
		this(Collections.emptyList(), message);
	}

	public List<Token> getTokensList() {
		return Collections.unmodifiableList(this.tokens);
	}

	/**
	 * @deprecated Use {@link #getTokensList()} instead.
	 */
	@Deprecated
	public Token[] getTokens() {
		return this.getTokensList().toArray(new Token[0]);
	}
}

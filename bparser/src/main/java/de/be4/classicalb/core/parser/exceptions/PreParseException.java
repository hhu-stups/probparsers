package de.be4.classicalb.core.parser.exceptions;

import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.preparser.node.Token;

@SuppressWarnings("serial")
public class PreParseException extends Exception {

	private final List<Token> tokens;
	// These fields are used when position info is known,
	// but there is no token object (e. g. for lexer exceptions)
	// or the token doesn't come from the PreParser and thus has the wrong class.
	private final int line;
	private final int pos;

	public PreParseException(final List<Token> tokens, final String message, final Throwable cause) {
		super(message, cause);
		this.tokens = tokens;
		if (tokens.isEmpty()) {
			this.line = 0;
			this.pos = 0;
		} else {
			Token token = tokens.get(0);
			this.line = token.getLine();
			this.pos = token.getPos();
		}
	}

	public PreParseException(int line, int pos, String message, Throwable cause) {
		super(message, cause);
		this.tokens = Collections.emptyList();
		this.line = line;
		this.pos = pos;
	}

	public PreParseException(final Token token, final String message, final Throwable cause) {
		this(Collections.singletonList(token), message, cause);
	}

	public PreParseException(final String message, final Throwable cause) {
		this(Collections.emptyList(), message, cause);
	}

	public PreParseException(final List<Token> tokens, final String message) {
		this(tokens, message, null);
	}

	public PreParseException(final Token token, final String message) {
		this(Collections.singletonList(token), message);
	}

	public PreParseException(int line, int pos, String message) {
		this(line, pos, message, null);
	}

	public PreParseException(final String message) {
		this(Collections.emptyList(), message);
	}

	public List<Token> getTokensList() {
		return Collections.unmodifiableList(this.tokens);
	}

	public int getLine() {
		return this.line;
	}

	public int getPos() {
		return this.pos;
	}
}

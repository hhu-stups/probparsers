package de.be4.classicalb.core.parser.exceptions;

import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.preparser.node.TSomething;
import de.be4.classicalb.core.preparser.node.Token;

@SuppressWarnings("serial")
public class PreParseException extends Exception {

	private final List<Token> tokens;

	public PreParseException(final List<Token> tokens, final String message, final Throwable cause) {
		super(message, cause);
		this.tokens = tokens;
	}

	public PreParseException(int line, int pos, String message, Throwable cause) {
		this(Collections.singletonList(new TSomething("", line, pos)), message, cause);
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
		return this.getTokensList().isEmpty() ? 0 : this.getTokensList().get(0).getLine();
	}

	public int getPos() {
		return this.getTokensList().isEmpty() ? 0 : this.getTokensList().get(0).getPos();
	}
}

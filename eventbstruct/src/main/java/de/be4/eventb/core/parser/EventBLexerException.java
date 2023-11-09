package de.be4.eventb.core.parser;

import de.be4.eventb.core.parser.lexer.LexerException;
import de.be4.eventb.core.parser.node.Token;

@SuppressWarnings("serial")
public class EventBLexerException extends LexerException {

	private final Token lastToken;
	private final String lastText;
	private final int lastLine;
	private final int lastPos;

	public EventBLexerException(final Token token, final String message) {
		this(token, message, token.getText(), token.getLine(), token
				.getPos());
	}

	public EventBLexerException(final Token lastToken, final String message,
			final String lastText, final int lastLine, final int lastPos) {
		super(message);
		this.lastToken = lastToken;
		this.lastText = lastText;
		this.lastLine = lastLine;
		this.lastPos = lastPos;
	}

	public String getLastText() {
		return lastText;
	}

	public int getLastLine() {
		return lastLine;
	}

	public int getLastPos() {
		return lastPos;
	}

	public Token getLastToken() {
		return lastToken;
	}

	@Override
	public String toString() {
        return super.toString() + " (" +
			lastLine +
			" / " +
			lastPos +
			": " +
			"'" +
			(lastToken != null ? lastToken.getText() : "[token unknown]") +
			"', '" +
			lastText +
			"')";
	}
}

package de.be4.classicalb.core.parser.exceptions;

import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Token;

@SuppressWarnings("serial")
public class BLexerException extends LexerException {

	private final Token lastToken;
	private final String lastText;

	public BLexerException(Token lastToken, String message, String lastText, int line, int pos) {
		super(line, pos, message);
		this.lastToken = lastToken;
		this.lastText = lastText;
	}

	public BLexerException(Token lastToken, String message) {
		this(lastToken, message, lastToken.getText(), lastToken.getLine(), lastToken.getPos());
	}

	public String getLastText() {
		return lastText;
	}

	public int getLastLine() {
		return this.getLine();
	}

	public int getLastPos() {
		return this.getPos();
	}

	public Token getLastToken() {
		return lastToken;
	}
}

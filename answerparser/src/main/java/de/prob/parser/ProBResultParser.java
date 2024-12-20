/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen,
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.parser;

import de.prob.core.sablecc.lexer.Lexer;
import de.prob.core.sablecc.lexer.LexerException;
import de.prob.core.sablecc.node.Start;
import de.prob.core.sablecc.parser.Parser;
import de.prob.core.sablecc.parser.ParserException;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

public final class ProBResultParser {

	private static final int PUSHBACK_READER_SIZE = 1;

	private ProBResultParser() {
		throw new UnsupportedOperationException("not intended for instantiation");
	}

	public static Start parse(final String prologAnswer) {
		if (prologAnswer == null || prologAnswer.isEmpty()) {
			throw new ResultParserException("Received empty Result");
		}

		final PushbackReader codeReader = new PushbackReader(new StringReader(prologAnswer), PUSHBACK_READER_SIZE);
		final Lexer lexer = new Lexer(codeReader);
		final Parser parser = new Parser(lexer);

		Start parseResult;
		try {
			parseResult = parser.parse();
		} catch (ParserException e) {
			String message = "Internal Error while parsing ProB answer. This is most likely a bug in the Result-Parser. String was: '" + prologAnswer + "'. Last Token was '" + e.getToken() + "': " + e.getMessage();
			throw new ResultParserException(message, e);
		} catch (LexerException | IOException e) {
			String message = "Internal Error while parsing ProB answer. This is most likely a bug in the Result-Parser. String was: '" + prologAnswer + "': " + e.getMessage();
			throw new ResultParserException(message, e);
		}

		return parseResult;
	}
}

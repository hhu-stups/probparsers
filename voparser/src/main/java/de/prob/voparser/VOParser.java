package de.prob.voparser;

import de.prob.voparser.lexer.Lexer;
import de.prob.voparser.lexer.LexerException;
import de.prob.voparser.node.Start;
import de.prob.voparser.parser.Parser;
import de.prob.voparser.parser.ParserException;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

public class VOParser {

	public Start parseFormula(String formula) throws VOParseException {
		return parseAST(formula);
	}

	public Start parseAST(String formula) throws VOParseException {
		StringReader reader = new StringReader(formula);
		PushbackReader r = new PushbackReader(reader);
		Lexer l = new Lexer(r);
		Parser p = new Parser(l);
		Start ast = null;
		try {
			ast = p.parse();
		} catch (ParserException e) {
			throw new VOParseException("Parsing VO formula failed");
		} catch (IOException e) {
			throw new VOParseException("Parsing VO formula failed");
		} catch (LexerException e) {
			throw new VOParseException("Parsing VO formula failed");
		}
		return ast;
	}

	// TODO: Implement semantic checks

}

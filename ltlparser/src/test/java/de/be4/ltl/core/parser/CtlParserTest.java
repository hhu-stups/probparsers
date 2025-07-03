package de.be4.ltl.core.parser;

import java.io.PushbackReader;
import java.io.StringReader;

import de.be4.ltl.core.ctlparser.parser.ParserException;
import de.be4.ltl.core.ctlparser.lexer.Lexer;
import de.be4.ltl.core.ctlparser.parser.Parser;

import org.junit.Test;

public final class CtlParserTest {
	@Test(expected = ParserException.class)
	public void ticket_parserlib_11_ctl() throws Exception {
		new Parser(new Lexer(new PushbackReader(new StringReader("AG {taken= {}")))).parse();
	}
}

package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;

import org.junit.Before;
import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class ErrorMessagesTest {
	private BParser parser;

	@Before
	public void setUp() {
		parser = new BParser("testcase");
		parser.getOptions().setGrammar(RulesGrammar.getInstance());
	}

	@Test
	public void testKeywordAsIdentifierInSigma() {
		String testMachine = "SIGMA(a, bool).(a= 1& bool= 2| {1} )";
		BParseException e = Helpers.assertThrowsCompound(BParseException.class, () -> parser.parseExpression(testMachine));
		assertEquals("bool", e.getToken().getText());
	}

	@Test
	public void testKeywordAsIdentifierInConstantsClause() {
		String testMachine = "MACHINE Test CONSTANTS bool PROPERTIES bool = 1 END";
		BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> parser.parseMachine(testMachine));
		// this is now caught as an invalid token combination classicalb.core.parser.exceptions.BLexerException
		//BParseException e1 = (BParseException) e.getFirstException().getCause();
		//assertEquals("bool", e1.getToken().getText());
		// TO DO: maybe check contents
	}

	@Test
	public void testKeywordAsIdentifierInUnitDeclaration() {
		String testMachine = "MACHINE Test CONSTANTS /*@unit*/ bool PROPERTIES bool = 1 END";
		BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> parser.parseMachine(testMachine));
		// this is now caught as an invalid token combination classicalb.core.parser.exceptions.BLexerException
		//BParseException e1 = (BParseException) e.getFirstException().getCause();
		//assertEquals("bool", e1.getToken().getText());
	}
}

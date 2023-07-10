package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class ErrorMessagesTest {

	@Test
	public void testKeywordAsIdentifierInSigma() {
		final String testMachine = "#EXPRESSION \nSIGMA(a, bool).(a= 1& bool= 2| {1} )";
		final BParseException e = Helpers.assertThrowsCompound(BParseException.class, () -> parseString(testMachine));
		assertEquals("bool", e.getToken().getText());
	}

	@Test
	public void testKeywordAsIdentifierInConstantsClause() {
		final String testMachine = "MACHINE Test CONSTANTS bool PROPERTIES bool = 1 END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> parseString(testMachine));
		// this is now caught as an invalid token combination classicalb.core.parser.exceptions.BLexerException
		//BParseException e1 = (BParseException) e.getFirstException().getCause();
		//assertEquals("bool", e1.getToken().getText());
		// TO DO: maybe check contents
	}

	@Test
	public void testKeywordAsIdentifierInUnitDeclaration() {
		final String testMachine = "MACHINE Test CONSTANTS /*@unit*/ bool PROPERTIES bool = 1 END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> parseString(testMachine));
		// this is now caught as an invalid token combination classicalb.core.parser.exceptions.BLexerException
		//BParseException e1 = (BParseException) e.getFirstException().getCause();
		//assertEquals("bool", e1.getToken().getText());
	}

	private void parseString(final String testMachine) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		parser.getOptions().setGrammar(RulesGrammar.getInstance());
		final Start startNode = parser.parseMachine(testMachine);
	}
}

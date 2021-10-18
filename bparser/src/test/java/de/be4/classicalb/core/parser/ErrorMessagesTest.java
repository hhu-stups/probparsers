package de.be4.classicalb.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.Start;
import util.Ast2String;

public class ErrorMessagesTest {

	@Test
	public void testKeywordAsIdentifierInSigma() throws Exception {
		final String testMachine = "#EXPRESSION \nSIGMA(a, left).(a= 1& left= 2| {1} )";
		try {
			parseString(testMachine);
			fail("Invalid identifier not detected");
 		} catch (BCompoundException e) {
 			//BParseException e1 = (BParseException) e.getFirstException().getCause();
 			// assertEquals("left", e1.getToken().getText());
		    // this is now caught as an invalid token combination classicalb.core.parser.exceptions.BLexerException
			// TO DO: maybe check contents
		}
	}

	@Test
	public void testKeywordAsIdentifierInConstantsClause() throws Exception {
		final String testMachine = "MACHINE Test CONSTANTS right PROPERTIES right = 1 END";
		try {
			parseString(testMachine);
			fail("Invalid identifier not detected");
		} catch (BCompoundException e) {
		    // this is now caught as an invalid token combination classicalb.core.parser.exceptions.BLexerException
			//BParseException e1 = (BParseException) e.getFirstException().getCause();
			//assertEquals("right", e1.getToken().getText());
			// TO DO: maybe check contents
		}
	}

	@Test
	public void testKeywordAsIdentifierInUnitDeclaration() throws Exception {
		final String testMachine = "MACHINE Test CONSTANTS /*@unit*/ right PROPERTIES right = 1 END";
		try {
			parseString(testMachine);
			fail("Invalid identifier not detected");
		} catch (BCompoundException e) {
		    // this is now caught as an invalid token combination classicalb.core.parser.exceptions.BLexerException
			//BParseException e1 = (BParseException) e.getFirstException().getCause();
			//assertEquals("right", e1.getToken().getText());
		}
	}

	private void parseString(final String testMachine) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		parser.getOptions().setGrammar(RulesGrammar.getInstance());
		final Start startNode = parser.parse(testMachine, false);
	}
}

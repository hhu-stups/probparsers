package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SyntaxErrorsDetectedOnTokenStreamTest {

	@Test
	public void checkForDuplicateSemicolon() {
		String s = "MACHINE DuplicateSemicolon\nOPERATIONS\n Foo = BEGIN skip END;\n ;r <-- Get = BEGIN r := xx END\nEND";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertTrue(e.getMessage().contains("Two succeeding"));
	}
	
	@Test
	public void checkForBeginAtEOF() {
		String s = "MACHINE BeginAtEOF\nOPERATIONS\n Foo = BEGIN";
		BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertTrue(e.getMessage().contains("Invalid combination of symbols"));
		assertTrue(e.getMessage().contains("BEGIN"));
		assertTrue(e.getMessage().contains("before the end of file"));
		assertFalse(e.getMessage().contains("before the end of definition"));
	}
	
	@Test
	public void checkForClauseAfterConjunction() {
		String s = "MACHINE Definitions\nPROPERTIES\n 1=1 & VARIABLES";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertTrue(e.getMessage().contains("Invalid combination of symbols"));
		assertTrue(e.getMessage().contains("&"));
		assertTrue(e.getMessage().contains("VARIABLES"));
		// message now is: Invalid combination of symbols: & VARIABLES . Argument to binary operator is missing.
	}

	@Test
	public void checkForDuplicateAnd() {
		String s = "MACHINE Definitions\nPROPERTIES\n 1=1 &\n &  2 = 2  END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(s));
		// checking the position of the second &
		assertEquals(4, e.getLastLine());
		assertEquals(2, e.getLastPos());
	}
	
	@Test
	public void checkForCommentBetweenDuplicateAnd() {
		String s = "MACHINE Definitions\nPROPERTIES 1=1 & /* comment */\n &  2 = 2  END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertTrue(e.getMessage().contains("'&' and '&'"));
		// checking the position of the second &
		assertEquals(3, e.getLastLine());
		assertEquals(2, e.getLastPos());
	}
	
	@Test
	public void checkForSingleLineCommentBetweenDuplicateAnd() {
		String s = "MACHINE Definitions\nPROPERTIES 1=1 & // comment 1 comment \n &  2 = 2  END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertTrue(e.getMessage().contains("'&' and '&'"));
	}
	
	@Test
	public void checkForDublicateAndInDefinitionsClause() {
		String s = "MACHINE Definitions\nDEFINITIONS\n foo == 1=1 && 2=2  \nEND";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertTrue(e.getMessage().contains("'&' and '&'"));
		assertEquals(3, e.getLine());
		assertEquals(14, e.getPos());
	}
	
	@Test
	public void checkForDublicateAndInDefinitionsClause2() {
		String s = "MACHINE Definitions \n DEFINITIONS\n foo == \n \n 1=1 \n&    & 2=2  \nEND";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertTrue(e.getMessage().contains("'&' and '&'"));
		assertEquals(6, e.getLine());
		assertEquals(6, e.getPos());
	}
	
	
	@Test
	public void checkForDublicateDefinitionClause() {
		String s = "MACHINE Definitions \n DEFINITIONS\n foo == 1\n CONSTANTS k \n DEFINITIONS\n bar == 1  \nEND";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getMachineAsPrologTerm(s));
		assertEquals("Clause 'DEFINITIONS' is used more than once", e.getMessage());
		assertEquals(5, e.getLine());
		assertEquals(2, e.getPos());
	}
}

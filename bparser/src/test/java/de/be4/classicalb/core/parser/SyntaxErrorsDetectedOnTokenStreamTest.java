package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SyntaxErrorsDetectedOnTokenStreamTest {

	@Test
	public void checkForDuplicateSemicolon() {
		String s = "MACHINE DuplicateSemicolon\nOPERATIONS\n Foo = BEGIN skip END;\n ;r <-- Get = BEGIN r := xx END\nEND";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getTreeAsString(s));
		assertTrue(e.getMessage().contains("Two succeeding"));
	}
	
	
	@Test
	public void checkForClauseAfterConjunction() {
		String s = "MACHINE Definitions\nPROPERTIES\n 1=1 & VARIABLES";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getTreeAsString(s));
		assertTrue(e.getMessage().contains("Invalid combination of symbols"));
		assertTrue(e.getMessage().contains("&"));
		assertTrue(e.getMessage().contains("VARIABLES"));
		// message now is: Invalid combination of symbols: & VARIABLES . Argument to binary operator is missing.
	}

	@Test
	public void checkForDuplicateAnd() {
		String s = "MACHINE Definitions\nPROPERTIES\n 1=1 &\n &  2 = 2  END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getTreeAsString(s));
		// checking the position of the second &
		assertEquals(4, e.getLastLine());
		assertEquals(2, e.getLastPos());
	}
	
	@Test
	public void checkForCommentBetweenDuplicateAnd() {
		String s = "MACHINE Definitions\nPROPERTIES 1=1 & /* comment */\n &  2 = 2  END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getTreeAsString(s));
		assertTrue(e.getMessage().contains("'&' and '&'"));
		// checking the position of the second &
		assertEquals(3, e.getLastLine());
		assertEquals(2, e.getLastPos());
	}
	
	@Test
	public void checkForSingleLineCommentBetweenDuplicateAnd() {
		String s = "MACHINE Definitions\nPROPERTIES 1=1 & // comment 1 comment \n &  2 = 2  END";
		final BLexerException e = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getTreeAsString(s));
		assertTrue(e.getMessage().contains("'&' and '&'"));
	}
	
	@Test
	public void checkForDublicateAndInDefinitionsClause() {
		String s = "MACHINE Definitions\nDEFINITIONS\n foo == 1=1 && 2=2  \nEND";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getTreeAsString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[3,14]"));
		assertTrue(e.getMessage().contains("'&' and '&'"));
	}
	
	@Test
	public void checkForDublicateAndInDefinitionsClause2() {
		String s = "MACHINE Definitions \n DEFINITIONS\n foo == \n \n 1=1 \n&    & 2=2  \nEND";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getTreeAsString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[6,6]"));
		assertTrue(e.getMessage().contains("'&' and '&'"));
	}
	
	
	@Test
	public void checkForDublicateDefinitionClause() {
		String s = "MACHINE Definitions \n DEFINITIONS\n foo == 1\n CONSTANTS k \n DEFINITIONS\n bar == 1  \nEND";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getTreeAsString(s));
		assertTrue(e.getMessage().contains("[5,2] Clause 'DEFINITIONS' is used more than once"));
	}
}

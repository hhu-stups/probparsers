package de.be4.classicalb.core.parser;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StringTest {

	@Test
	public void testFile() throws IOException, BCompoundException {
		String file = "strings/StringIncludingQuotes.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains("'a\"b'"));
	}

	@Test
	public void testString() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES \"a\\\"b\" = \"a\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("'a\"b'"));
	}

	@Test
	public void testNewlineInSingleLineString() {
		final String testMachine = "MACHINE Test PROPERTIES k = \" \n \" END";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		String message = e.getFirstException().getMessage();
		assertTrue(message.contains("Unknown token: \""));
	}

	@Test
	public void testDoubleBackslash() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\ ''' = ''' \\\\ ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,' \\\\ '),string(none,' \\\\ ')))]"));
	}

	@Test
	public void testNewline() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\n ''' = ''' \n ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,' \\n '),string(none,' \\n ')))]"));
	}

	@Test
	public void testTab() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\t ''' = ''' \t ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("string(none,' \\11\\ '),string(none,' \\11\\ '))"));
	}

	@Test
	public void testCarriageReturn() throws BCompoundException {
		// \r and \r\n will be transformed into a single \n
		// \n will stay the same
		final String testMachine = "MACHINE Test PROPERTIES '''a\r\nb\rc\nd''' = \"b\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("string(none,'a\\nb\\nc\\nd')"));
	}

	@Test
	public void testEscapedCarriageReturn() throws BCompoundException {
		// ...those same characters in their escaped variants will not trigger normalization
		final String testMachine = "MACHINE Test PROPERTIES '''a\\r\\nb\\rc\\nd\r\\ne\\r\nf''' = \"b\" END";
		String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("string(none,'a\\15\\\\nb\\15\\c\\nd\\n\\ne\\15\\\\nf')"));
	}

	@Test
	public void testSingleQuote() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\' ''' = ''' ' ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,' \\' '),string(none,' \\' '))"));
	}

	@Test
	public void testDoubleQuote() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\\" ''' = ''' \" ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,' \" '),string(none,' \" '))"));
	}

	@Test
	public void testBackslashAtEnd1() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES '''\\'''' = \"'\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,'\\''),string(none,'\\'')))]"));
	}

	@Test
	public void testBackslashAtEnd2() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES '''\\\\''' = \"\\\\\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,'\\\\'),string(none,'\\\\'))"));
	}

	@Test
	public void testBackslashAtEnd3() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES '''\\\\\\'''' = \"\\\\'\" END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(none,equal(none,string(none,'\\\\\\''),string(none,'\\\\\\'')))]"));
	}

	@Test
	public void testMultiLineString() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES k = ''' adfa \"a\" ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("' adfa \"a\" '"));
	}

	@Test
	public void testMultiLineString2() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES k = ''' adfa \"a ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("' adfa \"a '"));
	}

	@Test
	public void testStringLiteralNotClosedShortString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed END";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals("Unknown token: \"not closed END", e.getMessage());
		Helpers.assertParseErrorLocation(e, 1, 59, 1, 59);
	}

	@Test
	public void testStringLiteralNotClosedLongString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed"
				+ "'%'''%*__abcdezABCZäîüß ab 12ab ==> <===> !@#$%^&*()(]{[||]};;; "
				+ "== DEFINITIONS a=== MACHINE <<> ~`DEFINITIONS` ''  "
				+ "12345678999911112334234234345236245634563456345635463465345634563456345346534563546 "
				+ "END";
		PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getMessage(), e.getMessage().contains("Unknown token:"));
	}
}

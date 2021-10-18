package de.be4.classicalb.core.parser.prolog;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StringTest {

	@Test
	public void testFile() {
		String file = "strings/StringIncludingQuotes.mch";
		String result = Helpers.fullParsing(file);
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
		try {
			Helpers.getMachineAsPrologTerm(testMachine);
			fail("Should raise a parser exception");
		} catch (BCompoundException e) {
			String message = e.getFirstException().getMessage();
			assertTrue(message.contains("Unknown token: \""));
		}
	}

	@Test
	public void testDoubleBackslash() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\ ''' = ''' \\\\ ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(4,equal(5,string(6,' \\\\ '),string(7,' \\\\ ')))]"));
	}

	@Test
	public void testNewline() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\n ''' = ''' \n ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("[properties(4,equal(5,string(6,' \\n '),string(7,' \\n ')))]"));
	}

	@Test
	public void testTab() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\t ''' = ''' \t ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("string(6,' \\11\\ '),string(7,' \\11\\ '))"));
	}

	@Test
	public void testCarriageReturn() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\r ''' = ''' \r ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(5,string(6,' \\15\\ '),string(7,' \\15\\ '))"));
	}

	@Test
	public void testSignleQuote() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\' ''' = ''' ' ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(5,string(6,' \\' '),string(7,' \\' '))"));
	}

	@Test
	public void testDoubleQuote() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES ''' \\\" ''' = ''' \" ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(5,string(6,' \" '),string(7,' \" '))"));
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
}

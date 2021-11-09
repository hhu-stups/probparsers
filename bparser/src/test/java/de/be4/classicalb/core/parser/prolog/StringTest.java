package de.be4.classicalb.core.parser.prolog;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

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
		final String testMachine = "MACHINE Test PROPERTIES ''' \\r ''' = ''' \r ''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,string(none,' \\15\\ '),string(none,' \\15\\ '))"));
	}

	@Test
	public void testSignleQuote() throws BCompoundException {
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

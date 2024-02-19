package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StringLiteralNotClosedTest {

	@Test
	public void testStringLiteralNotClosedShortString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed END";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals("[1,59] Unknown token: \"not closed END", e.getMessage());
	}

	@Test
	public void testStringLiteralNotClosedLongString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed"
				//+ randomString(100) // could accidentally close the string !
				+ "'%'''%*__abcdezABCZäîüß ab 12ab ==> <===> !@#$%^&*()(]{[||]};;; "
				+ "== DEFINITIONS a=== MACHINE <<> ~`DEFINITIONS` ''  "
				+ "12345678999911112334234234345236245634563456345635463465345634563456345346534563546 "
				+ "END";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		// System.out.println("Exception = " + e.getLocalizedMessage());
		assertTrue(e.getLocalizedMessage(),e.getLocalizedMessage().contains("Unknown token:"));
	}

// 	String randomString(final int length) {
// 		Random r = new Random(); // perhaps make it a class variable so you
// 									// don't make a new one every time
// 		StringBuilder sb = new StringBuilder();
// 		for (int i = 0; i < length; i++) {
// 			char c = (char) (r.nextInt((int) (Character.MAX_VALUE)));
// 			sb.append(c);
// 		}
// 		return sb.toString();
// 	}

}

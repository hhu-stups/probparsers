package de.be4.classicalb.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import de.be4.classicalb.core.parser.analysis.Ast2String;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.extensions.RuleGrammar;
import de.be4.classicalb.core.parser.node.Start;

public class StringLiteralNotClosedTest {

	@Test
	public void testStringLiteralNotClosedShortString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed END";
		try {
			getTreeAsString(testMachine);
			fail("Exception did not occur");
		} catch (BException e) {
			assertEquals("[1,59] Unknown token: \"not closed END", e.getMessage());
		}
	}

	@Test
	public void testStringLiteralNotClosedLongString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed"
				+ randomString(100) + "END";
		try {
			getTreeAsString(testMachine);
			fail("Exception did not occur");
		} catch (BException e) {
			assertEquals("Parsing failed with an IOException: Pushback buffer overflow", e.getMessage());
		}
	}

	private String getTreeAsString(final String testMachine) throws BException {
		// System.out.println("Parsing \"" + testMachine + "\"");
		final BParser parser = new BParser("testcase");
		parser.getOptions().grammar = RuleGrammar.getInstance();
		final Start startNode = parser.parse(testMachine, false);

		// startNode.apply(new ASTPrinter());
		final Ast2String ast2String = new Ast2String();
		startNode.apply(ast2String);
		final String string = ast2String.toString();
		// System.out.println(string);
		return string;
	}

	String randomString(final int length) {
		Random r = new Random(); // perhaps make it a class variable so you
									// don't make a new one every time
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char c = (char) (r.nextInt((int) (Character.MAX_VALUE)));
			sb.append(c);
		}
		return sb.toString();
	}

}
package de.be4.classicalb.core.parser;

import java.util.Random;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import util.Ast2String;
import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StringLiteralNotClosedTest {

	@Test
	public void testStringLiteralNotClosedShortString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed END";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> getTreeAsString(testMachine));
		assertEquals("[1,59] Unknown token: \"not closed END", e.getMessage());
	}

	@Test
	public void testStringLiteralNotClosedLongString() {
		final String testMachine = "MACHINE Test CONSTANTS the_string PROPERTIES the_string = \"not closed"
				+ randomString(100) + "END";
		final PreParseException e = Helpers.assertThrowsCompound(PreParseException.class, () -> getTreeAsString(testMachine));
		assertTrue(e.getLocalizedMessage().contains("Unknown token:"));
	}

	private String getTreeAsString(final String testMachine) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		parser.getOptions().setGrammar(RulesGrammar.getInstance());
		final Start startNode = parser.parse(testMachine, false);

		final Ast2String ast2String = new Ast2String();
		startNode.apply(ast2String);
		return ast2String.toString();
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

package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.lexer.LexerException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class PrimedIdentifierTest {
	@Test
	public void testBecomeSuchSubstitution() throws BCompoundException {
		String testSubstitution = "x : (x$0 = x)";
		String expected = "machine(becomes_such(none,[identifier(none,x)],equal(none,primed_identifier(none,x,0),identifier(none,x)))).";
		String actual = Helpers.getSubstitutionAsPrologTerm(testSubstitution);
		assertEquals(expected, actual);
	}

	@Test
	public void testRestrictedUsage() {
		String testSubstitution = "x : (x = x$5)";
		Helpers.assertThrowsCompound(LexerException.class, () -> Helpers.getSubstitutionAsPrologTerm(testSubstitution));
	}

	@Test
	public void testDontPrimedIdentifiersInQuantifiers() {
		String testPredicate = "!a$0.(a=5 => b=6)";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getPredicateAsPrologTerm(testPredicate));
	}

	@Test
	public void testPrimedIdentifiersInQuantifiers() throws BCompoundException {
		String testPredicate = "!a$0.(a$0=5 => b=6)";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getPredicateAsPrologTerm(testPredicate));
	}
}

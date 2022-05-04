package de.be4.classicalb.core.parser.languageextension;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class LetPredicateTest {
	@Test
	public void testSingleIdentifierLetPredicate() throws BCompoundException {
		final String testMachine = "#PREDICATE (LET x BE x = 5 IN x < 7 END)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(let_predicate(none,[identifier(none,x)],equal(none,identifier(none,x),integer(none,5)),less(none,identifier(none,x),integer(none,7)))).",
				result);
	}
}

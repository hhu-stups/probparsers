package de.be4.classicalb.core.parser.languageextension;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class LetPredicateTest {
	@Test
	public void testSingleIdentifierLetPredicate() throws BCompoundException {
		final String testMachine = "#PREDICATE (LET x BE x = 5 IN x < 7 END)";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(APredicateParseUnit(ALetPredicatePredicate(AIdentifierExpression([x])AEqualPredicate(AIdentifierExpression([x]),AIntegerExpression(5))ALessPredicate(AIdentifierExpression([x]),AIntegerExpression(7)))))",
				result);
	}
}

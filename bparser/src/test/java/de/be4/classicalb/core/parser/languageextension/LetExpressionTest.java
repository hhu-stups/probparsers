package de.be4.classicalb.core.parser.languageextension;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.Helpers;

public class LetExpressionTest {
	@Test
	public void testSingleIdentifierLetExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION (LET x BE x = 5 IN x+1 END)";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(AExpressionParseUnit(ALetExpressionExpression(AIdentifierExpression([x])AEqualPredicate(AIdentifierExpression([x]),AIntegerExpression(5))AAddExpression(AIdentifierExpression([x]),AIntegerExpression(1)))))",
				result);
	}

	@Test
	public void testMultipleIdentifiersLetExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION (LET x, y BE x = 5 & y = 7 IN x+y END)";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(AExpressionParseUnit(ALetExpressionExpression(AIdentifierExpression([x])AIdentifierExpression([y])AConjunctPredicate(AEqualPredicate(AIdentifierExpression([x]),AIntegerExpression(5)),AEqualPredicate(AIdentifierExpression([y]),AIntegerExpression(7)))AAddExpression(AIdentifierExpression([x]),AIdentifierExpression([y])))))",
				result);
	}
}

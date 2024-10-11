package de.be4.classicalb.core.parser.languageextension;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.Helpers;

public class LetExpressionTest {
	@Test
	public void testSingleIdentifierLetExpression() throws BCompoundException {
		String testExpression = "(LET x BE x = 5 IN x+1 END)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(let_expression(none,[identifier(none,x)],equal(none,identifier(none,x),integer(none,5)),add(none,identifier(none,x),integer(none,1)))).",
				result);
	}

	@Test
	public void testMultipleIdentifiersLetExpression() throws BCompoundException {
		String testExpression = "(LET x, y BE x = 5 & y = 7 IN x+y END)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(let_expression(none,[identifier(none,x),identifier(none,y)],conjunct(none,[equal(none,identifier(none,x),integer(none,5)),equal(none,identifier(none,y),integer(none,7))]),add(none,identifier(none,x),identifier(none,y)))).",
				result);
	}
}

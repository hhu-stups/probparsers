package de.be4.classicalb.core.parser.languageextension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import util.Helpers;

public class IfThenElseExpressionTest {

	@Test
	public void testIfThenElseExpression() throws Exception {
		String testExpression = "IF x < 3 THEN 5 ELSE 17 END";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);
		assertTrue(result.contains("if_then_else(none,less(none,identifier(none,x),integer(none,3)),integer(none,5),integer(none,17))"));
	}

	@Test
	public void testIfThenElseSubstitution() throws Exception {
		String testSubstitution = "IF x < 3 THEN skip ELSIF 1=1 THEN skip ELSE skip END";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);
	}

	@Test
	public void testElseIf() throws Exception {
		String testExpression = "IF x < 1 THEN 2 ELSIF x= 3 THEN 4 ELSE 5 END";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);
		assertEquals("machine(if_then_else(none,less(none,identifier(none,x),integer(none,1)),integer(none,2),if_then_else(none,equal(none,identifier(none,x),integer(none,3)),integer(none,4),integer(none,5)))).", result);
	}

}

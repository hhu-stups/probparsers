package de.be4.classicalb.core.parser;

import org.junit.Test;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class FormulaTest {
	@Test
	public void testFormulaOutput() throws BCompoundException {
		String output = Helpers.getFormulaAsPrologTerm("1+1");
		assertEquals(output, "machine(add(none,integer(none,1),integer(none,1))).");
	}

	@Test
	public void testFomulaExpression() throws Exception {
		String formula = "3 + y";
		new BParser().parseFormula(formula);
	}

	@Test
	public void testFomulaPredicate() throws Exception {
		String formula = "3 = y";
		new BParser().parseFormula(formula);
	}

	@Test(expected = BCompoundException.class)
	public void testBuggyFomulaExpression() throws Exception {
		String formula = "3 + y - ";
		new BParser().parseFormula(formula);
	}

	@Test(expected = BCompoundException.class)
	public void testBuggyFomulaPredicate() throws Exception {
		String formula = "3 = ";
		new BParser().parseFormula(formula);
	}

	// Ensure that JIRA ticket PROB-295 is fixed.
	@Test
	public void ticketProb295() throws BCompoundException {
		// String input = "#x. /* comment */ (x>1000 & x<2**10)";
		String input1 = "#x. /*buh */ (  x>1000 & x<2**10)";
		String input2 = "#x.(/*buh */ x>1000 & x<2**10)";

		String result1 = Helpers.getFormulaAsPrologTerm(input1);
		String result2 = Helpers.getFormulaAsPrologTerm(input2);

		assertEquals(result1, result2);
	}
}

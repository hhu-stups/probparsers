package de.be4.classicalb.core.parser;

import org.junit.Test;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

public class FormulaTest {

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

}

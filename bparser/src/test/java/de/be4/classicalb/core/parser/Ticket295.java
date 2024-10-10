package de.be4.classicalb.core.parser;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class Ticket295 {

	// #x. /* comment */ (x>1000 & x<2**10)
	@Test
	public void ticker295() throws Exception {

		// String input = "#x. /* comment */ (x>1000 & x<2**10)";
		String input1 = "#x. /*buh */ (  x>1000 & x<2**10)";
		String input2 = "#x.(/*buh */ x>1000 & x<2**10)";

		String result1 = Helpers.getFormulaAsPrologTerm(input1);
		String result2 = Helpers.getFormulaAsPrologTerm(input2);

		assertEquals(result1, result2);

	}
}

package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class ConsoleTest {

	@Test
	public void testFormulaOutput() throws BCompoundException {
		String output = Helpers.getFormulaAsPrologTerm("1+1");
		assertEquals(output, "machine(add(none,integer(none,1),integer(none,1))).");
	}

}

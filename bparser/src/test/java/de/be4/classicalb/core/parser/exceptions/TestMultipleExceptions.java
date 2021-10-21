package de.be4.classicalb.core.parser.exceptions;

import org.junit.Assert;
import org.junit.Test;

import util.Helpers;

public class TestMultipleExceptions {

	@Test
	public void testMultipleErrors() {
		final String testMachine = "./exceptions/MultipleErrors.mch";
		final BCompoundException e = Assert.assertThrows(BCompoundException.class, () -> Helpers.fullParsing(testMachine));
		Assert.assertEquals(2, e.getBExceptions().size());
		for (final BException ex : e.getBExceptions()) {
			Assert.assertEquals("Invalid semicolon after last substitution (before END)", ex.getMessage());
		}
	}
}

package de.be4.classicalb.core.parser.exceptions;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import util.Helpers;

public class LexerExceptionTest {

	@Test
	public void testLexerException() throws Exception {
		final String result = Helpers.fullParsing("exceptions/LexerStringError.mch");
		assertTrue(result.contains("LexerStringError.mch"));
		assertTrue(result.contains("[3,12]"));
	}


	@Test
	public void testLexerThrowsExceptionAndProvidesPositionInfo() throws Exception {
		try {
			Helpers.parseFile("exceptions/IfAndPredicates.mch");
		}
		catch (BCompoundException b){
			Assert.assertNotNull(b.getBExceptions().get(0));
			Assert.assertEquals(1, b.getBExceptions().get(0).getLocations().size());
		}

	}
}

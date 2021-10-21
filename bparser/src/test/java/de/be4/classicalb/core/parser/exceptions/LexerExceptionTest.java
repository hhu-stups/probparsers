package de.be4.classicalb.core.parser.exceptions;

import org.junit.Assert;
import org.junit.Test;

import util.Helpers;

public class LexerExceptionTest {

	@Test
	public void testLexerException() {
		final BCompoundException e = Assert.assertThrows(BCompoundException.class, () -> Helpers.fullParsing("exceptions/LexerStringError.mch"));
		Helpers.assertParseErrorLocation(e, 3, 12, 3, 12);
		Assert.assertTrue(e.getFirstException().getLocations().get(0).getFilename().contains("LexerStringError.mch"));
	}


	@Test
	public void testLexerThrowsExceptionAndProvidesPositionInfo() {
		final BCompoundException b = Assert.assertThrows(BCompoundException.class, () -> Helpers.parseFile("exceptions/IfAndPredicates.mch"));
		Helpers.assertParseErrorLocation(b, 18, 18, 18, 18);
	}
}

package de.be4.classicalb.core.parser.exceptions;

import org.junit.Assert;
import org.junit.Test;

import util.Helpers;

public class LexerExceptionTest {

	@Test
	public void testLexerException() {
		final BCompoundException e = Assert.assertThrows(BCompoundException.class, () -> Helpers.parseFile("exceptions/LexerStringError.mch"));
		Helpers.assertParseErrorLocation(e, 3, 12, 3, 12);
		Assert.assertTrue(e.getFirstException().getLocations().get(0).getFilename().contains("LexerStringError.mch"));
	}

	@Test
	public void testLexerExceptionWindows() {
		final BCompoundException e = Assert.assertThrows(BCompoundException.class, () -> Helpers.parseFile("exceptions/LexerStringError_win.mch"));
		Helpers.assertParseErrorLocation(e, 3, 12, 3, 12);
		Assert.assertTrue(e.getFirstException().getLocations().get(0).getFilename().contains("LexerStringError_win.mch"));
	}

	@Test
	public void testLexerThrowsExceptionAndProvidesPositionInfo() {
		final BCompoundException b = Assert.assertThrows(BCompoundException.class, () -> Helpers.parseFile("exceptions/IfAndPredicates.mch"));
		Helpers.assertParseErrorLocation(b, 18, 18, 18, 18);
	}

	@Test
	public void testLexerThrowsExceptionAndProvidesPositionInfoWindows() {
		final BCompoundException b = Assert.assertThrows(BCompoundException.class, () -> Helpers.parseFile("exceptions/IfAndPredicates_win.mch"));
		Helpers.assertParseErrorLocation(b, 18, 18, 18, 18);
	}
}

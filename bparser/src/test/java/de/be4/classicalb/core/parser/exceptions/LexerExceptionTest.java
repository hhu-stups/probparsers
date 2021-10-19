package de.be4.classicalb.core.parser.exceptions;

import org.junit.Assert;
import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertTrue;

public class LexerExceptionTest {

	@Test
	public void testLexerException() throws Exception {
		final String result = Helpers.fullParsing("exceptions/LexerStringError.mch");
		assertTrue(result.contains("LexerStringError.mch"));
		assertTrue(result.contains("[3,12]"));
	}


	@Test
	public void testLexerThrowsExceptionAndProvidesPositionInfo() {
		final BCompoundException b = Assert.assertThrows(BCompoundException.class, () -> Helpers.parseFile("exceptions/IfAndPredicates.mch"));
		Helpers.assertParseErrorLocation(b, 18, 18, 18, 18);
	}
}

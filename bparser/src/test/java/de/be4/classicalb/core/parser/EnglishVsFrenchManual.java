package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * The English manual of AtelierB (version 1.8.6) has wrong priorities. This
 * test case tries to exploit differences between the correct French version and
 * the incorrect English.
 */
public class EnglishVsFrenchManual {

	@Test
	public void testImplicationVsEquivalence() throws BCompoundException {
		// <=> has 30 in the English, and 60 in the French version
		// => has 30 in both
		final String pred = "(z:g) => (x:g) <=> (y:g)";
		final String english = "((z:g) => (x:g)) <=> (y:g)";
		final String french = "(z:g) => ((x:g) <=> (y:g))";
		checkPred(pred, english, french);
	}

	@Test
	public void testEquivalenceVsAnd() throws Exception {
		// <=> has 30 in the English, and 60 in the French version
		// & has 40 in both
		final String pred = "(1=2) <=> (3=4) & (5=6)";
		final String english = "1=2 <=> (3=4 & 5=6)";
		final String french = "((1=2) <=> (3=4)) & (5=6)";
		checkPred(pred, english, french);
	}

	private void checkPred(final String pred, final String english,
			final String french) throws BCompoundException {
		String parsedPred = Helpers.getPredicateAsPrologTerm(pred);
		String parsedEnglish = Helpers.getPredicateAsPrologTerm(english);
		String parsedFrench = Helpers.getPredicateAsPrologTerm(french);

		assertEquals(parsedFrench, parsedPred);
		assertNotEquals(parsedPred, parsedEnglish);
	}

	@Test
	public void testOverrideExpression() throws BCompoundException {
		// <+ has 90 in the English, and 160 in the French version
		// <-> has 125 in both
		final String expr = "A <+ B <-> B";
		final String english = "A <+ (B <-> B)";
		final String french = "(A <+ B) <-> B";

		String parsedExpr = Helpers.getExpressionAsPrologTerm(expr);
		String parsedEnglish = Helpers.getExpressionAsPrologTerm(english);
		String parsedFrench = Helpers.getExpressionAsPrologTerm(french);

		assertEquals(parsedFrench, parsedExpr);
		assertNotEquals(parsedExpr, parsedEnglish);
	}

}

package de.be4.classicalb.core.parser.analysis;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import util.Helpers;

public class ParseTestUtil {
	public static String parsePred(final String input) throws BCompoundException {
		return Helpers.getTreeAsString(BParser.PREDICATE_PREFIX + " " + input);
	}

	public static String parseExpr(final String input) throws BCompoundException {
		return Helpers.getTreeAsString(BParser.EXPRESSION_PREFIX + " " + input);
	}

	public static String createTripleExpr(final String op1, final String op2) {
		return "(A " + op1 + " B " + op2 + " C)";
	}

	public static String createTripleExprLeft(final String op1, final String op2) {
		return "((A " + op1 + " B) " + op2 + " C)";
	}

	public static String createTripleExprRight(final String op1,
			final String op2) {
		return "(A " + op1 + " (B " + op2 + " C))";
	}
}

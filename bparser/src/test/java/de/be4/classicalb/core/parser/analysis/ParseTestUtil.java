package de.be4.classicalb.core.parser.analysis;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import util.Helpers;

public class ParseTestUtil {
	public static String parsePred(final String input) throws BCompoundException {
		return Helpers.getTreeAsPrologTerm(new BParser().parsePredicate(input));
	}

	public static String parseExpr(final String input) throws BCompoundException {
		return Helpers.getTreeAsPrologTerm(new BParser().parseExpression(input));
	}
}

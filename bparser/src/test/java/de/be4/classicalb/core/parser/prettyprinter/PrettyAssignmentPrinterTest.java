package de.be4.classicalb.core.parser.prettyprinter;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PrettyAssignmentPrinterTest {

	private static final String PREFIX = "#SUBSTITUTION ";

	static final String[] tests = {
		"x:=3",
		"x,y:=y,x",
		"skip",
			"f(x) := E",
			"x :: S",
			"xxxx : (xxxx=iv)",
			"x,y :: S",
			"x <-- OP(x)",
			"x := 5 || skip",
			"u :: P || x,y := t,x || f(3) := 9",
			"u :: P ; x,y := t,x ; f(3) := 9",
			"ANY x,y WHERE x<8 & y > 4 THEN z := x * y END",
			"LET x,y BE x=3 & y=n IN z := x+y END",
			"VAR x,y IN t := f(x,y) END",
			"PRE p<9 THEN x := 9 END",
			"ASSERT x=9 THEN y := x + 1 END",
			"CHOICE x := 1 OR x := 2 END",
			"CHOICE x := 1 OR x := 2 OR t := 0 END",
			"IF P=9 THEN G:=3 END",
			"IF P=9 THEN G:=3 ELSE G := G -1 END",
			"IF P=9 THEN G:=3 ELSIF u < 9 THEN s := u ELSE G := G -1 END",
			"SELECT P=9 THEN G:=3 WHEN u < 9 THEN s := u END",
			"SELECT P=9 THEN G:=3 WHEN u < 9 THEN s := u ELSE skip END",
			"CASE t OF EITHER 1 THEN y:= 7 OR 2,3,4 THEN q := 9 END END",
			"CASE t OF EITHER 1 THEN y:= 7 OR 2,3,4 THEN q := 9 ELSE a := 0 END END",
			"WHILE x > 9 DO x := x - 1 INVARIANT x > 0 VARIANT x END",
			"BEGIN skip END",
			"BEGIN skip; x := 42 END",
			"BEGIN skip; BEGIN x := 42 END END",
			"BEGIN BEGIN skip END; x := 42 END",
			"BEGIN BEGIN skip END; BEGIN x := 42 END END",
	};

	String theString;

	public PrettyAssignmentPrinterTest(String theString) {
		this.theString = theString;
	}

	@Parameterized.Parameters(name = "{0}")
	public static String[] data() {
		return tests;
	}

	@Test
	public void testExpression() throws Exception {
		Start parse = BParser.parse(PREFIX + theString);
		PrettyPrinter prettyprinter = new PrettyPrinter();

		parse.apply(prettyprinter);
		String prettyPrint = prettyprinter.getPrettyPrint();
		Start parse2 = BParser.parse(PREFIX + prettyPrint);
		PrettyPrinter prettyprinter2 = new PrettyPrinter();

		parse2.apply(prettyprinter2);
		assertEquals(Helpers.getTreeAsPrologTerm(parse),
				Helpers.getTreeAsPrologTerm(parse2));
		assertEquals(prettyPrint, prettyprinter2.getPrettyPrint());
	}
}

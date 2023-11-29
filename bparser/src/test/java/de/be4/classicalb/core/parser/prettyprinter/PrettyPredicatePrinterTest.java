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
public class PrettyPredicatePrinterTest {

	private static final String PREFIX = "#PREDICATE ";

	static final String[] tests = {
			"x<y",
			"a<b&b<c",
			"x=y",
			"x:NAT",
			"A<:B",
			"A<<:B",
			"A/<:B",
			"A/<<:B",
			"x/=y",
			"x/:NAT",
			"x<=y",
			"x>y",
			"x>=y",
			"!X,Y.(X:NAT&Y:NAT=>x<y)",
			"#X,Y.(X:NAT&Y:NAT=>x<y)",
			"1=4 or 12=19",
			"1=4 => 12=19",
			"x=1 => y=2 => z=3",
			"x=1 => (y=2 => z=3)",
			"1=4 <=> 12=19",
			"not(7=3)",
			"@finite(B/\\A)",
			"@partition(S)",
			"@partition(S, {a})",
			"@partition(S, {a}, {b})",
			"@partition(S, {a}, {b}, {c,d}, {e,f,g})",
			"!y.(y:DOM => !(x1,x2).(x1:DOM & x1<x2 & x2:DOM  => (Board(x1)(y) /= Board(x2)(y) &	Board(y)(x1) /= Board(y)(x2))))&!(s1,s2).(s1:SUBSQ & s2:SUBSQ => !(x1,y1,x2,y2).( (x1:s1 & x2:s1 & x1>=x2 & (x1=x2 => y1>y2) & y1:s2 & y2:s2 & (x1,y1) /= (x2,y2)) => Board(x1)(y1) /= Board(x2)(y2)))",
			"!(i1,j1,i2,j2).(( i1>0 & i2>0 & j1<=n & j2 <= n & i1<j1 & i2<j2 & (i1,j1) /= (i2,j2) & i1<=i2 & (i1=i2 => j1<j2)) => (a(j1)-a(i1) /= a(j2)-a(i2)))",
			"x+1:NAT",
			"1=1 /*@desc truth */",
			"/*@label axm1 */ 1=1",
			"[a := 1] a=1",
			"btrue",
			"bfalse",
	};

	final String theString;

	public PrettyPredicatePrinterTest(String theString) {
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

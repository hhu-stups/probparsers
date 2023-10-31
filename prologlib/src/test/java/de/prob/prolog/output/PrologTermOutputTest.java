package de.prob.prolog.output;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeNoException;

@RunWith(Enclosed.class)
public class PrologTermOutputTest {

	private IPrologTermOutput pout;
	private StringWriter swriter;

	@Before
	public void setUp() {
		swriter = new StringWriter();
		pout = new PrologTermOutput(new PrintWriter(swriter), false);
	}

	private void assertOutput(final String expected) {
		pout.flush();
		assertEquals(expected, swriter.toString().trim());
	}

	@Test
	public void testTerms() {
		pout.openTerm("func");
		pout.printAtom("atom");
		pout.openTerm("inner");
		pout.printNumber(4500);
		pout.printNumber(22);
		pout.closeTerm();
		pout.printAtom("atom2");
		pout.closeTerm();
		pout.fullstop();
		assertOutput("func(atom,inner(4500,22),atom2).");
	}

	@Test
	public void testEscape() {
		pout.printAtom("normal");
		pout.printAtom("camelStyle");
		pout.printAtom("with_underscore");
		pout.printAtom("UpperCase");
		pout.printAtom("_begin_with_underscore");
		pout.printAtom("22number");
		pout.openTerm("Functor");
		pout.printAtom("with white spaces");
		pout.closeTerm();
		assertOutput("normal,camelStyle,with_underscore,'UpperCase','_begin_with_underscore','22number','Functor'('with white spaces')");
	}

	@Test
	public void testEscape2() {
		pout.printAtom("hallo\nwelt");
		pout.printAtom("back\\slash");
		pout.printAtom("\u00dcmlaute"); // U - Umlaut
		pout.printAtom(" donttrim ");
		pout.printAtom("apo'stroph");
		pout.printAtom("double\"quote");
		assertOutput("'hallo\\nwelt','back\\\\slash','\\334\\mlaute',' donttrim ','apo\\'stroph','double\"quote'");
	}

	@Test
	public void testEscape3() {
		pout.printAtom(" ");
		assertOutput("' '");
	}

	@Test
	public void testLists() {
		pout.openTerm("term");
		pout.openList();
		pout.printAtom("a");
		pout.printAtom("b");
		pout.openList();
		pout.printAtom("c");
		pout.closeList();
		pout.closeList();
		pout.openList();
		pout.closeList();
		pout.emptyList();
		pout.closeTerm();
		assertOutput("term([a,b,[c]],[],[])");
	}

	@Test
	public void testStrings() {
		pout.printString("simple");
		pout.printString("apo'stroph");
		pout.printString("double\"quote");
		assertOutput("\"simple\",\"apo'stroph\",\"double\\\"quote\"");
	}

	@Test
	public void testVariables() {
		pout.openTerm("bla");
		pout.printVariable("Var1");
		pout.printVariable("Var2WithCamel");
		pout.printVariable("Var_with_underscores");
		pout.printVariable("_beginning_with_underscore");
		pout.closeTerm();
		assertOutput("bla(Var1,Var2WithCamel,Var_with_underscores,_beginning_with_underscore)");
	}

	@Test
	public void testInvalidVariables() {
		assertThrows(IllegalArgumentException.class, () -> pout.printVariable("lowerCase"));
		assertThrows(IllegalArgumentException.class, () -> pout.printVariable("Having whitespace"));
	}

	@Test
	public void testInvalidLists1() {
		assertThrows(IllegalStateException.class, () -> {
			pout.openList();
			pout.printAtom("test");
			pout.fullstop();
		});
	}

	@Test
	public void testInvalidLists2() {
		assertThrows(IllegalStateException.class, () -> pout.closeList());
	}

	@Test
	public void testInvalidTerms1() {
		assertThrows(IllegalStateException.class, () -> {
			pout.openTerm("test");
			pout.printAtom("test");
			pout.printAtom("test");
			pout.fullstop();
		});
	}

	@Test
	public void testInvalidTerms2() {
		assertThrows(IllegalStateException.class, () -> pout.closeTerm());
	}

	@Test
	public void testAccents() {
		pout.printAtom("h\u00e4ll\u00f3");
		assertOutput("'h\\344\\ll\\363\\'");
	}

	@Test
	public void testZeroArity() {
		pout.openTerm("test");
		pout.closeTerm();
		assertOutput("test");
	}

	@RunWith(Parameterized.class)
	public static class AtomTest {

		@Parameterized.Parameter
		public String input;
		@Parameterized.Parameter(1)
		public String output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ "", "''" },
				{ "a", "a" },
				{ "foobarbaz", "foobarbaz" },
			};
		}

		@Test
		public void testAtom() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printAtom(input);
			assertEquals(output, sw.toString().trim());
		}

		@Test
		public void testTerm() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.openTerm(input);
			pto.closeTerm();
			assertEquals(output, sw.toString().trim());
		}
	}

	@RunWith(Parameterized.class)
	public static class IntegerTest {

		@Parameterized.Parameter
		public Object input;
		@Parameterized.Parameter(1)
		public String output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ 0, "0" },
				{ 1, "1" },
				{ -1, "-1" },
				{ BigInteger.ONE.shiftLeft(64), "18446744073709551616" },
			};
		}

		@Test
		public void testLong() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			if (input instanceof BigInteger) {
				long n;
				try {
					n = ((BigInteger) input).longValueExact();
				} catch (ArithmeticException e) {
					assumeNoException(e);
					return;
				}
				pto.printNumber(n);
			} else {
				long n = ((Number) input).longValue();
				pto.printNumber(n);
			}
			assertEquals(output, sw.toString().trim());
		}

		@Test
		public void testBigInteger() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			if (input instanceof BigInteger) {
				pto.printNumber((BigInteger) input);
			} else {
				BigInteger n = BigInteger.valueOf(((Number) input).longValue());
				pto.printNumber(n);
			}
			assertEquals(output, sw.toString().trim());
		}
	}

	@RunWith(Parameterized.class)
	public static class FloatTest {

		@Parameterized.Parameter
		public double input;
		@Parameterized.Parameter(1)
		public String output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ 0, "0.0" },
				{ 1, "1.0" },
				{ -1, "-1.0" },
			};
		}

		@Test
		public void test() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printNumber(input);
			assertEquals(output, sw.toString().trim());
		}
	}

	@RunWith(Parameterized.class)
	public static class StringTest {

		@Parameterized.Parameter
		public String input;
		@Parameterized.Parameter(1)
		public String output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ "", "\"\"" },
				{ "a", "\"a\"" },
				{ "foobarbaz", "\"foobarbaz\"" },
			};
		}

		@Test
		public void test() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printString(input);
			assertEquals(output, sw.toString().trim());
		}
	}

	@RunWith(Parameterized.class)
	public static class AtomOrNumberTest {

		@Parameterized.Parameter
		public String input;
		@Parameterized.Parameter(1)
		public String output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ "", "''" },
				{ "a", "a" },
				{ "foobarbaz", "foobarbaz" },
			};
		}

		@Test
		public void test() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printAtomOrNumber(input);
			assertEquals(output, sw.toString().trim());
		}
	}
}

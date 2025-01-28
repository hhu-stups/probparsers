package de.prob.prolog.output;

import java.io.StringWriter;
import java.math.BigInteger;

import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.FloatPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

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

	public static class NormalTest {

		private IPrologTermOutput pto;
		private StringWriter sw;

		@Before
		public void setUp() {
			sw = new StringWriter();
			pto = new PrologTermOutput(sw, false);
		}

		private void assertOutput(final String expected) {
			pto.flush();
			assertEquals(expected, sw.toString().trim());
		}

		@Test
		public void testAtomOrNumberAfterComma() {
			pto.printAtom("atom");
			pto.printAtomOrNumber("1234");
			pto.printAtomOrNumber("atom2");
			assertOutput("atom,1234,atom2");
		}

		@Test
		public void testInvalidTermListWeaving() {
			pto.openTerm("f");
			pto.openList();
			pto.closeTerm();
			pto.closeList();
			// TODO: do we want to catch this?
			assertOutput("f([)]");
		}

		@Test
		public void testTerms() {
			pto.openTerm("func");
			pto.printAtom("atom");
			pto.openTerm("inner");
			pto.printNumber(4500);
			pto.printNumber(22);
			pto.closeTerm();
			pto.printAtom("atom2");
			pto.closeTerm();
			pto.fullstop();
			assertOutput("func(atom,inner(4500,22),atom2).");
		}

		@Test
		public void testLists() {
			pto.openTerm("term");
			pto.openList();
			pto.printAtom("a");
			pto.printAtom("b");
			pto.openList();
			pto.printAtom("c");
			pto.closeList();
			pto.closeList();
			pto.openList();
			pto.closeList();
			pto.emptyList();
			pto.closeTerm();
			assertOutput("term([a,b,[c]],[],[])");
		}

		@Test
		public void testTailSeparator() {
			pto.openList();
			pto.printVariable("H1");
			pto.printVariable("H2");
			pto.tailSeparator();
			pto.printVariable("T");
			pto.closeList();
			assertOutput("[H1,H2|T]");
		}

		@Test
		public void testAnonVariable() {
			pto.openTerm("term");
			pto.printAnonVariable();
			pto.printAnonVariable();
			pto.closeTerm();
			assertOutput("term(_,_)");
		}

		@Test
		public void testInvalidVariables() {
			assertThrows(IllegalArgumentException.class, () -> pto.printVariable("lowerCase"));
			assertThrows(IllegalArgumentException.class, () -> pto.printVariable("Having whitespace"));
		}

		@Test
		public void testInvalidLists1() {
			assertThrows(IllegalStateException.class, () -> {
				pto.openList();
				pto.printAtom("test");
				pto.fullstop();
			});
		}

		@Test
		public void testInvalidLists2() {
			assertThrows(IllegalStateException.class, () -> pto.closeList());
		}

		@Test
		public void testInvalidTerms1() {
			assertThrows(IllegalStateException.class, () -> {
				pto.openTerm("test");
				pto.printAtom("test");
				pto.printAtom("test");
				pto.fullstop();
			});
		}

		@Test
		public void testInvalidTerms2() {
			assertThrows(IllegalStateException.class, () -> pto.closeTerm());
		}
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
				{ "fooBarBaz", "fooBarBaz" },
				{ "foo_bar_baz", "foo_bar_baz" },
				{ "foo0bar1baz", "foo0bar1baz" },
				{ "A", "'A'" },
				{ "Foobarbaz", "'Foobarbaz'" },
				{ "FooBarBaz", "'FooBarBaz'" },
				{ " ", "' '" },
				{ " a ", "' a '" },
				{ "foo bar baz", "'foo bar baz'" },
				{ "1", "'1'" },
				{ "1.0", "'1.0'" },
				{ "1foo2bar3baz4", "'1foo2bar3baz4'" },
				{ "\n", "'\\n'" },
				{ "\\", "'\\\\'" },
				{ "ü", "'\\374\\'" },
				{ "Ü", "'\\334\\'" },
				{ "ó", "'\\363\\'" },
				{ "'", "'\\''" },
				{ "\"", "'\"'" },
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
		public void testEmptyFunctor() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.openTerm(input);
			pto.closeTerm();
			assertEquals(output, sw.toString().trim());
		}

		@Test
		public void testEmptyFunctorWithIndentation() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.openTerm(input, false);
			pto.closeTerm();
			assertEquals(output, sw.toString().trim());
		}

		@Test
		public void testEmptyFunctorWithIgnoreIndentation() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.openTerm(input, true);
			pto.closeTerm();
			assertEquals(output, sw.toString().trim());
		}

		@Test
		public void testZeroArityTerm() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printTerm(new CompoundPrologTerm(input));
			assertEquals(output, sw.toString().trim());
		}
	}

	@RunWith(Parameterized.class)
	public static class VariableTest {

		@Parameterized.Parameter
		public String input;
		@Parameterized.Parameter(1)
		public String output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ "A", "A" },
				{ "Ab", "Ab" },
				{ "A1", "A1" },
				{ "_a", "_a" },
				{ "_A", "_A" },
				{ "__", "__" },
				{ "_1", "_1" },
				{ "Abc", "Abc" },
				{ "AbC", "AbC" },
				{ "Ab2", "Ab2" },
				{ "Ab_", "Ab_" },
				{ "ABc", "ABc" },
				{ "ABC", "ABC" },
				{ "AB2", "AB2" },
				{ "AB_", "AB_" },
				{ "A_c", "A_c" },
				{ "A_C", "A_C" },
				{ "A_2", "A_2" },
				{ "A__", "A__" },
			};
		}

		@Test
		public void testVariable() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printVariable(input);
			assertEquals(output, sw.toString().trim());
		}

		@Test
		public void testTerm() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printTerm(new VariablePrologTerm(input));
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
				{ Integer.MAX_VALUE, "2147483647" },
				{ Integer.MIN_VALUE, "-2147483648" },
				{ Long.MAX_VALUE, "9223372036854775807" },
				{ Long.MIN_VALUE, "-9223372036854775808" },
				{ BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE), "170141183460469231731687303715884105727" },
				{ BigInteger.ONE.shiftLeft(127).negate(), "-170141183460469231731687303715884105728" },
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

		@Test
		public void testTerm() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			if (input instanceof BigInteger) {
				pto.printTerm(AIntegerPrologTerm.create((BigInteger) input));
			} else {
				pto.printTerm(AIntegerPrologTerm.create(((Number) input).longValue()));
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
				{ Float.MAX_VALUE, "3.4028234663852886E38" },
				{ -Float.MAX_VALUE, "-3.4028234663852886E38" },
				{ Float.MIN_VALUE, "1.401298464324817E-45" },
				{ -Float.MIN_VALUE, "-1.401298464324817E-45" },
				{ Double.MAX_VALUE, "1.7976931348623157E308" },
				{ -Double.MAX_VALUE, "-1.7976931348623157E308" },
				{ Double.MIN_VALUE, "4.9E-324" },
				{ -Double.MIN_VALUE, "-4.9E-324" },
			};
		}

		@Test
		public void testFloat() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printNumber(input);
			assertEquals(output, sw.toString().trim());
		}

		@Test
		public void testTerm() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printTerm(new FloatPrologTerm(input));
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
				{ "fooBarBaz", "\"fooBarBaz\"" },
				{ "foo_bar_baz", "\"foo_bar_baz\"" },
				{ "foo0bar1baz", "\"foo0bar1baz\"" },
				{ "A", "\"A\"" },
				{ "Foobarbaz", "\"Foobarbaz\"" },
				{ "FooBarBaz", "\"FooBarBaz\"" },
				{ " ", "\" \"" },
				{ " a ", "\" a \"" },
				{ "foo bar baz", "\"foo bar baz\"" },
				{ "1", "\"1\"" },
				{ "1.0", "\"1.0\"" },
				{ "1foo2bar3baz4", "\"1foo2bar3baz4\"" },
				{ "\n", "\"\\n\"" },
				{ "\\", "\"\\\\\"" },
				{ "ü", "\"\\374\\\"" },
				{ "Ü", "\"\\334\\\"" },
				{ "ó", "\"\\363\\\"" },
				{ "'", "\"'\"" },
				{ "\"", "\"\\\"\"" },
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
				{ "white space", "'white space'" },
				{ "0", "0" },
				{ "1", "1" },
				{ "-1", "-1" },
				{ String.valueOf(Integer.MAX_VALUE), "2147483647" },
				{ String.valueOf(Integer.MIN_VALUE), "-2147483648" },
				{ String.valueOf(Long.MAX_VALUE), "9223372036854775807" },
				{ String.valueOf(Long.MIN_VALUE), "-9223372036854775808" },
				{ BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE).toString(), "'170141183460469231731687303715884105727'" },
				{ BigInteger.ONE.shiftLeft(127).negate().toString(), "'-170141183460469231731687303715884105728'" },
				{ "1.0", "'1.0'" },
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

	@RunWith(Parameterized.class)
	public static class TermTest {

		@Parameterized.Parameter
		public PrologTerm input;
		@Parameterized.Parameter(1)
		public String output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ ListPrologTerm.emptyList(), "[]" },
				{ new ListPrologTerm(AIntegerPrologTerm.create(1)), "[1]" },
				{ new ListPrologTerm(AIntegerPrologTerm.create(1), AIntegerPrologTerm.create(2)), "[1,2]" },
				{ new CompoundPrologTerm("a"), "a" },
				{ new CompoundPrologTerm("a", AIntegerPrologTerm.create(1)), "a(1)" },
				{ new CompoundPrologTerm("a", AIntegerPrologTerm.create(1), AIntegerPrologTerm.create(2)), "a(1,2)" },
			};
		}

		@Test
		public void test() {
			StringWriter sw = new StringWriter();
			IPrologTermOutput pto = new PrologTermOutput(sw, false);
			pto.printTerm(input);
			assertEquals(output, sw.toString().trim());
		}
	}
}

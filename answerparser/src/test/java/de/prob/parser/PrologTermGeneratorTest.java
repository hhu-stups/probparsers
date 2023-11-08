package de.prob.parser;

import de.prob.prolog.term.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class PrologTermGeneratorTest {

	public static class NormalTest {

		@Test
		public void testListSemanticSWI() {
			PrologTerm expected = new CompoundPrologTerm("x", new ListPrologTerm(AIntegerPrologTerm.create(1), AIntegerPrologTerm.create(2), new CompoundPrologTerm("=", new CompoundPrologTerm("foo"), new CompoundPrologTerm("bar"))));
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse("yes(x(.(1,.(2,.(=(foo,bar),[])))))"));
			assertEquals(expected, actual);
		}

		@Test
		public void testListSemanticSICStus() {
			PrologTerm expected = new CompoundPrologTerm("x", new ListPrologTerm(AIntegerPrologTerm.create(1), AIntegerPrologTerm.create(2), new CompoundPrologTerm("=", new CompoundPrologTerm("foo"), new CompoundPrologTerm("bar"))));
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse("yes(x('.'(1,'.'(2,'.'(=(foo,bar),[])))))"));
			assertEquals(expected, actual);
		}

		@Test
		public void testListSemantic() {
			PrologTerm expected = new CompoundPrologTerm("x", new ListPrologTerm(AIntegerPrologTerm.create(1), AIntegerPrologTerm.create(2), new CompoundPrologTerm("=", new CompoundPrologTerm("foo"), new CompoundPrologTerm("bar"))));
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse("yes(x([1,2,=(foo,bar)]))"));
			assertEquals(expected, actual);
		}
	}

	@RunWith(Parameterized.class)
	public static class IntegerTest {

		@Parameterized.Parameter
		public Object input;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ 0 },
				{ 1 },
				{ 2 },
				{ 3 },
				{ 4 },
				{ 5 },
				{ 6 },
				{ 7 },
				{ 8 },
				{ 9 },
				{ 10 },
				{ 15 },
				{ 16 },
				{ 127 },
				{ 128 },
				{ 255 },
				{ 256 },
				{ Integer.MAX_VALUE },
				{ 1L << 31 },
				{ (1L << 32) - 1 },
				{ 1L << 32 },
				{ Long.MAX_VALUE },
				{ BigInteger.ONE.shiftLeft(63) },
				{ BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE) },
				{ BigInteger.ONE.shiftLeft(64) },
				{ BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE) },
				{ BigInteger.ONE.shiftLeft(127) },
				{ BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE) },
				{ BigInteger.ONE.shiftLeft(128) },
			};
		}

		private void test(String prefix, int radix) {
			boolean negative = prefix.startsWith("-");
			PrologTerm expected;
			String actualInput;
			if (input instanceof BigInteger) {
				BigInteger n = (BigInteger) input;
				expected = AIntegerPrologTerm.create(negative ? n.negate() : n);
				actualInput = n.toString(radix);
			} else {
				long n = ((Number) input).longValue();
				expected = AIntegerPrologTerm.create(negative ? Math.negateExact(n) : n);
				actualInput = Long.toString(n, radix);
			}

			String text = "yes(" + prefix + actualInput + ")";
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(text));
			assertEquals(expected, actual);
		}

		@Test
		public void testDec() {
			test("", 10);
		}

		@Test
		public void testDecPlus() {
			test("+", 10);
		}

		@Test
		public void testDecMinus() {
			test("-", 10);
		}

		@Test
		public void testHex() {
			test("0x", 16);
		}

		@Test
		public void testHexPlus() {
			test("+0x", 16);
		}

		@Test
		public void testHexMinus() {
			test("-0x", 16);
		}

		@Test
		public void testOct() {
			test("0o", 8);
		}

		@Test
		public void testOctPlus() {
			test("+0o", 8);
		}

		@Test
		public void testOctMinus() {
			test("-0o", 8);
		}

		@Test
		public void testBin() {
			test("0b", 2);
		}

		@Test
		public void testBinPlus() {
			test("+0b", 2);
		}

		@Test
		public void testBinMinus() {
			test("-0b", 2);
		}
	}

	@RunWith(Parameterized.class)
	public static class IntegerCharLiteralTest {

		@Parameterized.Parameter
		public String input;
		@Parameterized.Parameter(1)
		public int output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ "0'\\a", 7 },
				{ "0'\\b", 8 },
				{ "0'\\t", 9 },
				{ "0'\\n", 10 },
				{ "0'\\v", 11 },
				{ "0'\\f", 12 },
				{ "0'\\r", 13 },
				{ "0'\\e", 0x1b },
				{ "0'\\d", 0x7f },
				{ "0'\\\\", '\\' },
				{ "0'\\'", '\'' },
				{ "0'\\\"", '"' },
				{ "0'\\`", '`' },
				{ "0'\\x0\\", 0 },
				{ "0'\\x1\\", 1 },
				{ "0'\\x10\\", 16 },
				{ "0'\\0\\", 0 },
				{ "0'\\1\\", 1 },
				{ "0'\\10\\", 8 },
				{ "0'''", '\'' },
				{ "0' ", ' ' },
				{ "0'a", 'a' },
				{ "0'`", '`' },
			};
		}

		@Test
		public void testWithoutSign() {
			PrologTerm expected = AIntegerPrologTerm.create(output);
			String text = "yes(" + input + ")";
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(text));
			assertEquals(expected, actual);
		}

		@Test
		public void testPlus() {
			PrologTerm expected = AIntegerPrologTerm.create(output);
			String text = "yes(+" + input + ")";
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(text));
			assertEquals(expected, actual);
		}

		@Test
		public void testMinus() {
			PrologTerm expected = AIntegerPrologTerm.create(Math.negateExact(output));
			String text = "yes(-" + input + ")";
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(text));
			assertEquals(expected, actual);
		}
	}

	@RunWith(Parameterized.class)
	public static class FloatTest {

		@Parameterized.Parameter
		public Object input;
		@Parameterized.Parameter(1)
		public Object output;

		@Parameterized.Parameters
		public static Object[][] data() {
			return new Object[][] {
				{ "0.0", 0 },
				{ "1.0", 1 },
				{ "1e0", 1 },
				{ "1.0e0", 1 },
				{ "1E0", 1 },
				{ "1.0E0", 1 },
			};
		}

		@Test
		public void testWithoutSign() {
			double d = ((Number) output).doubleValue();
			PrologTerm expected = new FloatPrologTerm(d);
			String text = "yes(" + input + ")";
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(text));
			assertEquals(expected, actual);
		}

		@Test
		public void testPlus() {
			double d = ((Number) output).doubleValue();
			PrologTerm expected = new FloatPrologTerm(d);
			String text = "yes(+" + input + ")";
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(text));
			assertEquals(expected, actual);
		}

		@Test
		public void testMinus() {
			double d = ((Number) output).doubleValue();
			PrologTerm expected = new FloatPrologTerm(-d);
			String text = "yes(-" + input + ")";
			PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(text));
			assertEquals(expected, actual);
		}
	}
}

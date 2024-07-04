package de.prob.prolog.output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.prob.prolog.term.PrologTerm;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FastReadWriterTest {

	private final StructuredPrologOutput pto = new StructuredPrologOutput();

	@Test
	public void testSingleNumber() {
		pto.printNumber(42);
		check("DI42\0");
	}

	@Test
	public void testSingleAtom1() {
		pto.printAtom("a");
		check("DAa\0");
	}

	@Test
	public void testSingleAtom2() {
		pto.printAtom("foobar");
		check("DAfoobar\0");
	}

	@Test
	public void testSingleAtom3() {
		pto.printAtom("C");
		check("DAC\0");
	}

	@Test
	public void testSingleAtom4() {
		pto.printAtom("\uD83D\uDE09");
		check("DA\360\237\230\211\0");
	}

	@Test
	public void testSimpleFunctor1() { // a(b)
		pto.openTerm("a").printAtom("b").closeTerm();
		check("DSa\0\1Ab\0");
	}

	@Test
	public void testSimpleFunctor2() { // 'C'(a)
		pto.openTerm("C").printAtom("a").closeTerm();
		check("DSC\0\1Aa\0");
	}

	@Test
	public void testSingleVariable() {
		pto.printVariable("Foo");
		check("D_0\0");
	}

	@Test
	public void testMultiVariable() {
		pto.openList().printVariable("X").printVariable("X").closeList();
		check("D[_0\0[_0\0]");
	}

	@Test
	public void testSharedVariables() {
		pto.openList().printVariable("X").printVariable("X").printVariable("Y").printVariable("Y").closeList();
		check("D[_0\0[_0\0[_1\0[_1\0]");
	}

	@Test
	public void testEmptyList() {
		pto.emptyList();
		check("D]");
	}

	@Test
	public void testOneAtomList() {
		pto.openList().printAtom("C").closeList();
		check("D[AC\0]");
	}

	@Test
	public void testComplex() { // a(['G',f([]),[[w]]]).
		pto.openTerm("a").openList();
		pto.printAtom("G");
		pto.openTerm("f").openList().closeList().closeTerm();
		pto.openList().openList().printAtom("w").closeList().closeList();
		pto.closeList().closeTerm();
		check("DSa\0\1[AG\0[Sf\0\1][[[Aw\0]]]");
	}

	@Test
	public void testLargeCompound() {
		pto.openTerm("large");
		// SICStus max_arity is 255, which is also the maximum possible in the fastrw format.
		for (int i = 0; i < 255; i++) {
			pto.emptyList();
		}
		pto.closeTerm();
		// 377oct is 255dec
		// Append 255 ']' characters at the end:
		check("DSlarge\0\377" + new String(new char[255]).replace('\0', ']'));
	}

	@Test
	public void testSWISingleNumber() {
		pto.printNumber(42);
		checkSWI(new byte[] { 0x76, 0x01, 0x2a });
	}

	@Test
	public void testSWISingleNumberBig() {
		pto.printNumber(1L << 60);
		checkSWI(new byte[] { 0x76, 0x08, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
	}

	@Test
	public void testSWISingleAtom() {
		pto.printAtom("foo");
		checkSWI(new byte[] { 0x7a, 0x0b, 0x03, 0x66, 0x6f, 0x6f });
	}

	@Test
	public void testSWISingleAtomUnicode() {
		pto.printAtom("\uD83D\uDE09");
		checkSWI(new byte[] { 0x7a, 0x0c, 0x04, 0x09, (byte) 0xf6, 0x01, 0x00 });
	}

	@Test
	public void testSWISingleAtomUnicodeWindows() {
		pto.printAtom("\uD83D\uDE09");
		checkSWIWindows(new byte[] { 0x7a, 0x0c, 0x04, 0x3d, (byte) 0xd8, 0x09, (byte) 0xde });
	}

	@Test
	public void testSWISingleEmptyList() {
		pto.emptyList();
		checkSWI(new byte[] { 0x7a, 0x09 });
	}

	@Test
	public void testSWISingleFloat() {
		pto.printNumber(1.337);
		checkSWI(new byte[] { 0x72, 0x09, 0x03, 0x0e, 0x31, 0x08, (byte) 0xac, 0x1c, 0x5a, 0x64, (byte) 0xf5, 0x3f });
	}

	@Test
	public void testSWISingleVariable() {
		pto.printVariable("X");
		checkSWI(new byte[] { 0x62, 0x02, 0x00, 0x01, 0x01, 0x00 });
	}

	@Test
	public void testSWICompoundTerm1() {
		pto.openTerm("a").printAtom("b").closeTerm();
		checkSWI(new byte[] { 0x72, 0x08, 0x02, 0x0d, 0x01, 0x0b, 0x01, 0x61, 0x0b, 0x01, 0x62 });
	}

	@Test
	public void testSWICompoundTerm2() {
		pto.openTerm("a").printAtom("b").printAtom("c").closeTerm();
		checkSWI(new byte[] { 0x72, 0x0b, 0x03, 0x0d, 0x02, 0x0b, 0x01, 0x61, 0x0b, 0x01, 0x62, 0x0b, 0x01, 0x63 });
	}

	@Test
	public void testSWIList1() {
		pto.openList().printAtom("a").closeList();
		checkSWI(new byte[] { 0x72, 0x05, 0x03, 0x08, 0x0b, 0x01, 0x61, 0x09 });
	}

	@Test
	public void testSWIList2() {
		pto.openList().printAtom("a").printAtom("b").closeList();
		checkSWI(new byte[] { 0x72, 0x09, 0x06, 0x08, 0x0b, 0x01, 0x61, 0x08, 0x0b, 0x01, 0x62, 0x09 });
	}

	@Test
	public void testSWISharedVariables() {
		pto.openList().printVariable("X").printVariable("X").printVariable("Y").printVariable("Y").closeList();
		checkSWI(new byte[] { 0x62, 0x0d, 0x0c, 0x02, 0x08, 0x01, 0x00, 0x08, 0x01, 0x00, 0x08, 0x01, 0x01, 0x08, 0x01, 0x01, 0x09 });
	}

	@Test
	public void testSWIComplex() { // a(['G',f([]),[[w, 42]]]).
		pto.openTerm("a").openList();
		pto.printAtom("G");
		pto.openTerm("f").openList().closeList().closeTerm();
		pto.openList().openList().printAtom("w").printNumber(42).closeList().closeList();
		pto.closeList().closeTerm();
		checkSWI(new byte[] {0x72, 0x1d, 0x16, 0x0d, 0x01, 0x0b, 0x01, 0x61, 0x08, 0x0b, 0x01, 0x47, 0x08, 0x0d, 0x01, 0x0b, 0x01, 0x66, 0x09, 0x08, 0x08, 0x08, 0x0b, 0x01, 0x77, 0x08, 0x04, 0x01, 0x2a, 0x09, 0x09, 0x09});
	}

	private void checkSWI(byte[] expected) {
		final PrologTerm term = pto.getLastTerm();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			// target a 64bit *nix system
			new FastReadWriter(FastReadWriter.PrologSystem.SWI, out)
					.withTarget64bit()
					.withTargetLittleEndian()
					.withTargetNoWindows()
					.fastwrite(term);
		} catch (IOException e) {
			throw new AssertionError("IOException while writing to in-memory stream, this should never happen", e);
		}
		byte[] actual = out.toByteArray();
		assertArrayEquals(expected, actual);
	}

	private void checkSWIWindows(byte[] expected) {
		final PrologTerm term = pto.getLastTerm();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			// target a 64bit windows system
			new FastReadWriter(FastReadWriter.PrologSystem.SWI, out)
					.withTarget64bit()
					.withTargetLittleEndian()
					.withTargetWindows()
					.fastwrite(term);
		} catch (IOException e) {
			throw new AssertionError("IOException while writing to in-memory stream, this should never happen", e);
		}
		byte[] actual = out.toByteArray();
		assertArrayEquals(expected, actual);
	}

	private void check(String expected) {
		final PrologTerm term = pto.getLastTerm();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new FastReadWriter(out).fastwrite(term);
		} catch (IOException e) {
			throw new AssertionError("IOException while writing to in-memory stream, this should never happen", e);
		}
		// Do the comparison using strings in ISO 8859-1.
		// This is functionally identical to comparing raw byte arrays,
		// but gives more readable errors when there is a mismatch,
		// because fastrw data is mostly valid ASCII.
		String actual = new String(out.toByteArray(), StandardCharsets.ISO_8859_1);
		assertEquals(expected, actual);
	}
}

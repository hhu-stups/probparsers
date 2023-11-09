package de.be4.classicalb.core.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;

import org.junit.Assert;
import org.junit.Test;

public class FastWriteTest {
	private final StructuredPrologOutput spo = new StructuredPrologOutput();
	
	@Test
	public void testSingleNumber() {
		spo.printNumber(42);
		spo.fullstop();
		String expected = "DI42\0";
		check(expected);
	}


	@Test
	public void testSingleAtom1() {
		spo.openTerm("a").closeTerm();
		spo.fullstop();
		String expected = "DAa\0";
		check(expected);
	}

	@Test
	public void testSingleAtom3() {
		spo.openTerm("C").closeTerm();
		spo.fullstop();
		String expected = "DAC\0";
		check(expected);
	}

	@Test
	public void testSimpleFunctor2() {
		spo.openTerm("C").printAtom("a").closeTerm();
		spo.fullstop();
		String expected ="DSC\0\1Aa\0"; 
		check(expected);
	}

	@Test
	public void testSingleVariable() {
		spo.printVariable("Foo");
		spo.fullstop();
		String expected = "D_0\0";
		check(expected);
	}

	@Test
	public void testSimpleFunctor() { // a(b)
		spo.openTerm("a").printAtom("b").closeTerm();
		spo.fullstop();
		String expected = "DSa\0\1Ab\0";
		check(expected);
	}

	@Test
	public void testSingleAtom2() {
		spo.printAtom("a");
		spo.fullstop();
		String expected = "DAa\0";
		check(expected);
	}

	@Test
	public void testEmptyList() {
		spo.openList().closeList();
		spo.fullstop();
		String expected = "D]";
		check(expected);
	}

	@Test
	public void testOneAtomList() {
		spo.openList().printAtom("C").closeList();
		spo.fullstop();
		String expected = "D[AC\0]";
		check(expected);
	}

	@Test
	public void testComplex() { // a(['G',f([]),[[w]]]).
		spo.openTerm("a").openList();
		spo.printAtom("G");
		spo.openTerm("f").openList().closeList().closeTerm();
		spo.openList().openList().printAtom("w").closeList().closeList();
		spo.closeList().closeTerm();
		spo.fullstop();
		String expected = "DSa\0\1[AG\0[Sf\0\1][[[Aw\0]]]";
		check(expected);
	}
	
	@Test
	public void testLargeCompound() {
		spo.openTerm("large");
		// SICStus max_arity is 255, which is also the maximum possible in the fastrw format.
		for (int i = 0; i < 255; i++) {
			spo.openList();
			spo.closeList();
		}
		spo.closeTerm();
		spo.fullstop();
		// Append 255 ']' characters at the end:
		final String expected = "DSlarge\0\377" + new String(new char[255]).replace('\0', ']');
		check(expected);
	}
	
	private void check(String expected) {
		assert spo.getSentences().size() == 1;
		final PrologTerm term = spo.getSentences().get(0);
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
		Assert.assertEquals(expected, new String(out.toByteArray(), StandardCharsets.ISO_8859_1));
	}

}

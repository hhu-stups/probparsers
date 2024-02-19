package de.prob.prolog.output;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PrologTermStringOutputTest {

	private IPrologTermOutput pto;

	@Before
	public void setUp() {
		pto = new PrologTermStringOutput();
	}

	private void assertOutput(final String expected) {
		assertEquals(expected, pto.toString().trim());
	}

	@Test
	public void testTerms() {
		pto.openTerm("func")
			.printAtom("atom")
			.openTerm("inner")
			.printNumber(4500)
			.printNumber(22)
			.closeTerm()
			.printAtom("atom2")
			.closeTerm()
			.fullstop();
		assertOutput("func(atom,inner(4500,22),atom2).");
	}

	@Test
	public void testEscape() {
		pto.printAtom("normal")
			.printAtom("camelStyle")
			.printAtom("with_underscore")
			.printAtom("UpperCase")
			.printAtom("_begin_with_underscore")
			.printAtom("22number")
			.openTerm("Functor")
			.printAtom("with white spaces")
			.closeTerm();
		assertOutput("normal,camelStyle,with_underscore,'UpperCase'," + "'_begin_with_underscore','22number','Functor'('with white spaces')");
	}

	@Test
	public void testEscape2() {
		pto.printAtom("hallo\nwelt")
			.printAtom("back\\slash")
			.printAtom("Ümlaute")
			.printAtom(" donttrim ")
			.printAtom("apo'stroph")
			.printAtom("double\"quote");
		assertOutput("'hallo\\nwelt','back\\\\slash','\\334\\mlaute',' donttrim '," + "'apo\\'stroph','double\"quote'");
	}

	@Test
	public void testLists() {
		pto.openTerm("term")
			.openList()
			.printAtom("a")
			.printAtom("b")
			.openList()
			.printAtom("c")
			.closeList()
			.closeList()
			.openList()
			.closeList()
			.closeTerm();
		assertOutput("term([a,b,[c]],[])");
	}

	@Test
	public void testStrings() {
		pto.printString("simple")
			.printString("apo'stroph")
			.printString("double\"quote");
		assertOutput("\"simple\",\"apo'stroph\",\"double\\\"quote\"");
	}

	@Test
	public void testVariables() {
		pto.openTerm("bla")
			.printVariable("Var1")
			.printVariable("Var2WithCamel")
			.printVariable("Var_with_underscores")
			.printVariable("_beginning_with_underscore")
			.closeTerm();
		assertOutput("bla(Var1,Var2WithCamel,Var_with_underscores,_beginning_with_underscore)");
	}

	@Test
	public void testInvalidVariables() {
		assertThrows(IllegalArgumentException.class, () -> pto.printVariable("lowerCase"));
		assertThrows(IllegalArgumentException.class, () -> pto.printVariable("Having whitespace"));
	}

	@Test
	public void testZeroArity() {
		pto.openTerm("test").closeTerm();
		assertOutput("test");
	}
}

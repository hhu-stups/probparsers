package de.prob.parser;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class RoundtripTest {

	private static void testRoundtrip(PrologTerm expected) {
		PrologTermStringOutput pto = new PrologTermStringOutput();
		pto.term("yes", t1 -> t1.printTerm(expected));
		PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(pto.toString()));
		assertEquals(expected, actual);
	}

	private static void testRoundtrip(Consumer<? super IPrologTermOutput> termGenerator) {
		StructuredPrologOutput spto = new StructuredPrologOutput();
		termGenerator.accept(spto);
		testRoundtrip(spto.getLastTerm());
	}

	@Test
	public void testAtom() {
		testRoundtrip(pto -> pto.printAtom("a"));
	}

	@Test
	public void testVariable() {
		testRoundtrip(pto -> pto.printVariable("X"));
	}

	@Test
	public void testInt() {
		testRoundtrip(pto -> pto.printNumber(42));
	}

	@Test
	public void testEmptyList() {
		testRoundtrip(IPrologTermOutput::emptyList);
	}

	@Test
	public void testList() {
		testRoundtrip(pto -> pto.list(l1 -> {
			l1.printAtom("a");
			l1.printVariable("X");
			l1.printNumber(42);
			l1.emptyList();
		}));
	}

	@Test
	public void testTerm() {
		testRoundtrip(pto -> {
			pto.term("f", t1 -> {
				t1.printAtom("a");
				t1.printVariable("X");
				t1.printNumber(42);
				t1.emptyList();
				t1.list(l1 -> {
					l1.printAtom("a");
					l1.printVariable("X");
					l1.printNumber(42);
					l1.term("g", t2 -> t2.term("h", t3 -> t3.printAtom("i")));
					l1.emptyList();
				});
			});
		});
	}
}

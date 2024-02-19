package de.prob.parser;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.DotListConversion;
import de.prob.prolog.term.PrologTerm;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class DotListTest {

	private void test(Consumer<? super IPrologTermOutput> scope) {
		StructuredPrologOutput spto = new StructuredPrologOutput();
		scope.accept(spto);
		PrologTerm expected = spto.getLastTerm();

		PrologTerm dotListTerm = DotListConversion.asListConcatTerm(expected);
		PrologTermStringOutput ptso = new PrologTermStringOutput();
		ptso.term("yes", t -> t.printTerm(dotListTerm));
		PrologTerm actual = PrologTermGenerator.toPrologTerm(ProBResultParser.parse(ptso.toString()));

		assertEquals(expected, actual);
	}

	@Test
	public void testEmptyList() {
		test(IPrologTermOutput::emptyList);
	}

	@Test
	public void testList1() {
		test(pto -> pto.list(l1 -> l1.printAtom("a")));
	}

	@Test
	public void testListComplicated() {
		test(pto -> pto.list(l1 -> {
			l1.printAtom("a");
			l1.printString("foo");
			l1.printVariable("X");
			l1.printNumber(42);
			l1.emptyList();
			l1.list(l2 -> {
				l2.printAtom("a");
				l2.printString("foo");
				l2.printVariable("X");
				l2.printNumber(42);
				l2.term("g", t2 -> t2.term("h", t3 -> t3.printAtom("i")));
				l2.emptyList();
			});
		}));
	}
}

package de.prob.prolog.term;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.StructuredPrologOutput;
import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class DotListTest {

	private void test(PrologTerm expected) {
		PrologTerm dotListTerm = DotListConversion.asListConcatTerm(expected);
		PrologTerm actual = DotListConversion.asListTerm(dotListTerm);
		assertEquals(expected, actual);
	}

	private void test(Consumer<? super IPrologTermOutput> scope) {
		StructuredPrologOutput pto = new StructuredPrologOutput();
		scope.accept(pto);
		test(pto.getLastTerm());
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
			l1.printAtom("Foo");
			l1.printVariable("X");
			l1.printNumber(42);
			l1.emptyList();
			l1.list(l2 -> {
				l2.printAtom("a");
				l2.printAtom("Foo");
				l2.printVariable("X");
				l2.printNumber(42);
				l2.term("g", t2 -> t2.term("h", t3 -> t3.printAtom("i")));
				l2.emptyList();
			});
		}));
	}
}

package de.prob.prolog.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ListIterator;

import org.junit.Test;

import de.prob.prolog.output.PrologTermStringOutput;

public class ListPrologTermTest {
	@Test(expected = IllegalStateException.class)
	public void tailTestEmpty() {
		ListPrologTerm l = new ListPrologTerm(new PrologTerm[0]);
		l.tail(); // throws exception
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void headTestEmpty() {
		ListPrologTerm l = new ListPrologTerm(new PrologTerm[0]);
		l.head();// throws exception
	}

	@Test
	public void tailTest2() {
		ListPrologTerm l = new ListPrologTerm(new PrologTerm[] { new IntegerPrologTerm(42) });
		ListPrologTerm tail = l.tail();
		assertTrue(tail.isEmpty());
	}

	@Test
	public void tailTest3() {
		ListPrologTerm l = new ListPrologTerm(new PrologTerm[] { new IntegerPrologTerm(42), new IntegerPrologTerm(5) });
		ListPrologTerm tail = l.tail();
		assertFalse(tail.isEmpty());
		assertTrue(tail.tail().isEmpty());
	}

	@Test
	public void initTest1() {
		ListPrologTerm t1 = new ListPrologTerm(new PrologTerm[0]);
		ListPrologTerm t2 = new ListPrologTerm();
		assertEquals(t1, t2);
	}

	@Test
	public void initTest2() {
		ListPrologTerm t1 = new ListPrologTerm(
				new PrologTerm[] { new IntegerPrologTerm(42), new IntegerPrologTerm(5) });
		ListPrologTerm t2 = new ListPrologTerm(new IntegerPrologTerm(42), new IntegerPrologTerm(5));
		assertEquals(t1, t2);
	}

	@Test
	public void testLength() {
		ListPrologTerm t2 = new ListPrologTerm(createFixture(10));
		assertEquals(10, t2.size());
		ListPrologTerm t1 = t2.subList(1, 4);
		assertEquals(3, t1.size());
	}

	@Test
	public void testGet() {
		ListPrologTerm t1 = new ListPrologTerm(createFixture(10));
		assertEquals("4", PrologTerm.atomicString(t1.get(4)));
		ListPrologTerm t2 = t1.subList(1, 5);
		assertEquals("2", PrologTerm.atomicString(t2.get(1)));
		assertEquals("4", PrologTerm.atomicString(t2.get(3)));
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testGetOutOfBounds() {
		ListPrologTerm t1 = new ListPrologTerm(createFixture(10));
		assertEquals("4", PrologTerm.atomicString(t1.get(4)));
		ListPrologTerm t2 = t1.subList(1, 5);
		t2.get(7); // raise exception
	}

	@Test
	public void testToTermOutput() {
		ListPrologTerm term = new ListPrologTerm(createFixture(10)).subList(1, 5);
		PrologTermStringOutput output = new PrologTermStringOutput();
		term.toTermOutput(output);
		assertEquals("['1','2','3','4']", output.toString());
	}

	@Test
	public void testIterator() {
		ListPrologTerm term = new ListPrologTerm(createFixture(10)).subList(1, 3);
		ListIterator<PrologTerm> i = term.listIterator();
		assertFalse(i.hasPrevious());
		assertEquals(-1, i.previousIndex());
		assertEquals(0, i.nextIndex());
		assertEquals("1", PrologTerm.atomicString(i.next()));
		assertEquals("2", PrologTerm.atomicString(i.next()));
		assertFalse(i.hasNext());
		assertTrue(i.hasPrevious());
		assertEquals(2, i.nextIndex());
		assertEquals("2", PrologTerm.atomicString(i.previous()));
		assertEquals("1", PrologTerm.atomicString(i.previous()));
		assertTrue(i.hasNext());
	}

	@Test
	public void testEmptyIterator() {
		ListPrologTerm term = new ListPrologTerm();
		ListIterator<PrologTerm> i = term.listIterator();
		assertFalse(i.hasNext());
		assertFalse(i.hasPrevious());
		assertEquals(-1, i.previousIndex());
		assertEquals(0, i.nextIndex());
	}

	@Test
	public void testSingletonIterator() {
		ListPrologTerm term = new ListPrologTerm(new CompoundPrologTerm("foo"));
		ListIterator<PrologTerm> i = term.listIterator();
		assertTrue(i.hasNext());
		assertFalse(i.hasPrevious());
		assertEquals(-1, i.previousIndex());
		assertEquals(0, i.nextIndex());
		PrologTerm next = i.next();
		assertEquals("foo", PrologTerm.atomicString(next));
		PrologTerm previous = i.previous();
		assertEquals(previous, next);
	}

	@Test
	public void testMultiTail() {
		ListPrologTerm term = new ListPrologTerm(createFixture(3));
		PrologTerm h1 = term.head();
		PrologTerm h2 = term.tail().head();
		PrologTerm h3 = term.tail().tail().head();
		assertEquals("0", PrologTerm.atomicString(h1));
		assertEquals("1", PrologTerm.atomicString(h2));
		assertEquals("2", PrologTerm.atomicString(h3));
	}

	@Test
	public void testMultiTail2() {
		ListPrologTerm term = new ListPrologTerm(createFixture(3));
		ListPrologTerm term2 = term.tail();
		assertEquals("1", PrologTerm.atomicString(term2.head()));
		ListPrologTerm term3 = term2.tail();
		assertEquals("2", PrologTerm.atomicString(term3.head()));
	}

	@Test
	public void testLastIndexOf() {
		ListPrologTerm t1 = new ListPrologTerm(new PrologTerm[] { new IntegerPrologTerm(42), new IntegerPrologTerm(5),
				new IntegerPrologTerm(5), new IntegerPrologTerm(6) });
		assertEquals(2, t1.lastIndexOf(new IntegerPrologTerm(5)));
	}

	private PrologTerm[] createFixture(int size) {
		PrologTerm[] res = new PrologTerm[size];
		for (int i = 0; i < res.length; i++) {
			res[i] = new CompoundPrologTerm("" + i);
		}
		return res;
	}

}

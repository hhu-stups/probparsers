package de.prob.prolog.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ListIterator;

import org.junit.Test;

import de.prob.prolog.output.PrologTermStringOutput;

public class ListPrologTermTest {
	@Test(expected = IllegalStateException.class)
	public void tailTestEmpty() {
		ListPrologTerm l = new ListPrologTerm();
		l.tail(); // throws exception
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void headTestEmpty() {
		ListPrologTerm l = new ListPrologTerm();
		l.head();// throws exception
	}

	@Test
	public void tailTest2() {
		ListPrologTerm l = new ListPrologTerm(AIntegerPrologTerm.create(42));
		ListPrologTerm tail = l.tail();
		assertTrue(tail.isEmpty());
	}

	@Test
	public void tailTest3() {
		ListPrologTerm l = new ListPrologTerm(AIntegerPrologTerm.create(42), AIntegerPrologTerm.create(5));
		ListPrologTerm tail = l.tail();
		assertFalse(tail.isEmpty());
		assertTrue(tail.tail().isEmpty());
	}

	@Test
	public void initTest1() {
		ListPrologTerm t1 = new ListPrologTerm();
		ListPrologTerm t2 = new ListPrologTerm();
		assertEquals(t1, t2);
	}

	@Test
	public void initTest2() {
		ListPrologTerm t1 = new ListPrologTerm(AIntegerPrologTerm.create(42), AIntegerPrologTerm.create(5));
		ListPrologTerm t2 = new ListPrologTerm(AIntegerPrologTerm.create(42), AIntegerPrologTerm.create(5));
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
		assertEquals("4", t1.get(4).atomToString());
		ListPrologTerm t2 = t1.subList(1, 5);
		assertEquals("2", t2.get(1).atomToString());
		assertEquals("4", t2.get(3).atomToString());
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testGetOutOfBounds() {
		ListPrologTerm t1 = new ListPrologTerm(createFixture(10));
		assertEquals("4", t1.get(4).atomToString());
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
		assertEquals("1", i.next().atomToString());
		assertEquals("2", i.next().atomToString());
		assertFalse(i.hasNext());
		assertTrue(i.hasPrevious());
		assertEquals(2, i.nextIndex());
		assertEquals("2", i.previous().atomToString());
		assertEquals("1", i.previous().atomToString());
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
		assertEquals("foo", next.atomToString());
		PrologTerm previous = i.previous();
		assertEquals(previous, next);
	}

	@Test
	public void testMultiTail() {
		ListPrologTerm term = new ListPrologTerm(createFixture(3));
		PrologTerm h1 = term.head();
		PrologTerm h2 = term.tail().head();
		PrologTerm h3 = term.tail().tail().head();
		assertEquals("0", h1.atomToString());
		assertEquals("1", h2.atomToString());
		assertEquals("2", h3.atomToString());
	}

	@Test
	public void testMultiTail2() {
		ListPrologTerm term = new ListPrologTerm(createFixture(3));
		ListPrologTerm term2 = term.tail();
		assertEquals("1", term2.head().atomToString());
		ListPrologTerm term3 = term2.tail();
		assertEquals("2", term3.head().atomToString());
	}

	@Test
	public void testMultiSubList() {
		final ListPrologTerm list = new ListPrologTerm(createFixture(10));
		final ListPrologTerm subList = list.subList(1, 4);
		assertEquals(3, subList.size());
		assertEquals(new ListPrologTerm(new CompoundPrologTerm("1"), new CompoundPrologTerm("2"), new CompoundPrologTerm("3")), subList);
		assertNotEquals(new ListPrologTerm(new CompoundPrologTerm("1"), new CompoundPrologTerm("2"), new CompoundPrologTerm("3"), new CompoundPrologTerm("4")), subList);
		assertNotEquals(subList, new ListPrologTerm(new CompoundPrologTerm("1"), new CompoundPrologTerm("2")));
		final ListPrologTerm subSubList = subList.subList(1, 2);
		assertEquals(1, subSubList.size());
		assertEquals(new ListPrologTerm(new CompoundPrologTerm("2")), subSubList);
		assertNotEquals(new ListPrologTerm(new CompoundPrologTerm("2"), new CompoundPrologTerm("3")), subSubList);
		assertNotEquals(new ListPrologTerm(), subSubList);
	}

	@Test
	public void testLastIndexOf() {
		ListPrologTerm t1 = new ListPrologTerm(AIntegerPrologTerm.create(42), AIntegerPrologTerm.create(5), AIntegerPrologTerm.create(5), AIntegerPrologTerm.create(6));
		assertEquals(2, t1.lastIndexOf(AIntegerPrologTerm.create(5)));
	}

	private PrologTerm[] createFixture(int size) {
		PrologTerm[] res = new PrologTerm[size];
		for (int i = 0; i < res.length; i++) {
			res[i] = new CompoundPrologTerm("" + i);
		}
		return res;
	}

}

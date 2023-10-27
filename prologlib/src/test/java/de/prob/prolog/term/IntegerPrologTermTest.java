package de.prob.prolog.term;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class IntegerPrologTermTest {

	@SuppressWarnings("deprecation")
	private void compareHash(long number) {
		AIntegerPrologTerm a = new IntegerPrologTerm(BigInteger.valueOf(number));
		AIntegerPrologTerm b = new IntegerLongPrologTerm(number);
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testHashEquivalenceZero() {
		compareHash(0);
	}

	@Test
	public void testHashEquivalenceOne() {
		compareHash(1);
	}

	@Test
	public void testHashEquivalenceMinusOne() {
		compareHash(-1);
	}

	@Test
	public void testHashEquivalenceIntMax() {
		compareHash(Integer.MAX_VALUE);
	}

	@Test
	public void testHashEquivalenceIntMaxP1() {
		compareHash(Integer.MAX_VALUE + 1L);
	}

	@Test
	public void testHashEquivalenceUIntMax() {
		compareHash(0xffffffffL);
	}

	@Test
	public void testHashEquivalenceIntMin() {
		compareHash(Integer.MIN_VALUE);
	}

	@Test
	public void testHashEquivalenceIntMinM1() {
		compareHash(Integer.MIN_VALUE - 1L);
	}

	@Test
	public void testHashEquivalenceLongMax() {
		compareHash(Long.MAX_VALUE);
	}

	@Test
	public void testHashEquivalenceLongMin() {
		compareHash(Long.MIN_VALUE);
	}
}

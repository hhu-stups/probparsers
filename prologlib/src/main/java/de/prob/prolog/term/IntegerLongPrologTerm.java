/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import de.prob.prolog.output.IPrologTermOutput;

import java.math.BigInteger;

/**
 * Represents a Prolog integer which can be represented as long.
 * a variation of IntegerPrologTerm which avoids using a BigInteger reference
 */
public final class IntegerLongPrologTerm extends AIntegerPrologTerm {

	// think about adding a hash/BigInteger cache
	private final long value;

	/**
	 * @deprecated use {@link AIntegerPrologTerm#create(long)} instead
	 */
	@Deprecated
	public IntegerLongPrologTerm(final long value) {
		this.value = value;
	}

	@Override
	public String getFunctor() {
		return Long.toString(this.value);
	}

	@Override
	public BigInteger getValue() {
		return BigInteger.valueOf(this.value);
	}

	@Override
	public long longValueExact() {
		return this.value;
	}

	@Override
	public int intValueExact() {
		return Math.toIntExact(this.value);
	}

	@Override
	public void toTermOutput(final IPrologTermOutput pto) {
		pto.printNumber(this.value);
	}

	@Override
	public boolean equals(Object other) {
		// for optimization
		if (other instanceof IntegerLongPrologTerm) {
			return this.value == ((IntegerLongPrologTerm) other).value;
		}
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		// this is a copy of the BigInteger hashing routine
		// for optimization
		long v = this.value;
		boolean neg = v < 0;
		if (neg) {
			v = -v;
		}

		int h = (int) (v >>> 32);
		int hash = (int) (31 * h + (v & 0xffffffffL));
		return neg ? -hash : hash;
	}
}

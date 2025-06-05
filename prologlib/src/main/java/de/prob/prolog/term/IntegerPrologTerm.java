/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents a Prolog integer.
 */
public final class IntegerPrologTerm extends AIntegerPrologTerm {

	private final BigInteger value;

	/**
	 * @deprecated use {@link AIntegerPrologTerm#create(BigInteger)} instead
	 */
	@Deprecated
	public IntegerPrologTerm(final BigInteger value) {
		this.value = Objects.requireNonNull(value, "value");
	}

	/**
	 * @deprecated use {@link AIntegerPrologTerm#create(long)} instead
	 */
	@Deprecated
	public IntegerPrologTerm(final long value) {
		this.value = BigInteger.valueOf(value);
	}

	/**
	 * @deprecated use {@link AIntegerPrologTerm#create(BigInteger)} instead
	 */
	@Deprecated
	public IntegerPrologTerm(final byte[] arr) {
		this.value = new BigInteger(arr);
	}

	@Override
	public BigInteger getValue() {
		return this.value;
	}
}

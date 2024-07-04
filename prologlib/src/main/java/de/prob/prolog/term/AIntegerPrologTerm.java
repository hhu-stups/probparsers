/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import java.math.BigInteger;
import java.util.Objects;

/**
 * The abstract class representing a Prolog integer.
 * Can be a long or a BigInteger.
 * The string representation can be accessed via {@link PrologTerm#getFunctor()}.
 */
@SuppressWarnings({ "deprecation", "StaticInitializerReferencesSubClass" })
public abstract class AIntegerPrologTerm extends PrologTerm {


	public static final AIntegerPrologTerm ZERO;
	public static final AIntegerPrologTerm ONE;
	public static final AIntegerPrologTerm MINUS_ONE;
	public static final AIntegerPrologTerm TWO;
	public static final AIntegerPrologTerm TEN;

	private static final int MAX_CONSTANT = 16;
	private static final AIntegerPrologTerm[] POS_CACHE = new AIntegerPrologTerm[MAX_CONSTANT + 1];
	private static final AIntegerPrologTerm[] NEG_CACHE = new AIntegerPrologTerm[MAX_CONSTANT + 1];

	static {
		ZERO = new IntegerLongPrologTerm(0);
		for (int i = 1; i <= MAX_CONSTANT; i++) {
			POS_CACHE[i] = new IntegerLongPrologTerm(i);
			NEG_CACHE[i] = new IntegerLongPrologTerm(-i);
		}

		ONE = create(1);
		MINUS_ONE = create(-1);
		TWO = create(2);
		TEN = create(10);
	}

	public static AIntegerPrologTerm create(final long number) {
		if (number == 0) {
			return ZERO;
		} else if (number > 0 && number <= MAX_CONSTANT) {
			return POS_CACHE[(int) number];
		} else if (number < 0 && number >= -MAX_CONSTANT) {
			return NEG_CACHE[(int) -number];
		}
		return new IntegerLongPrologTerm(number);
	}

	@SuppressWarnings("deprecation")
	public static AIntegerPrologTerm create(final BigInteger number) {
		Objects.requireNonNull(number, "number");
		try {
			return create(number.longValueExact());
		} catch (ArithmeticException ignored) {
			return new IntegerPrologTerm(number);
		}
	}

	public static AIntegerPrologTerm create(String number) {
		return create(number, 10);
	}

	public static AIntegerPrologTerm create(String number, int radix) {
		Objects.requireNonNull(number, "number");
		try {
			return create(Long.parseLong(number, radix));
		} catch (NumberFormatException ignored) {
			return create(new BigInteger(number, radix));
		}
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public boolean isInteger() {
		return true;
	}

	public abstract BigInteger getValue();

	/**
	 * Get this integer's value as a {@code long},
	 * checking for overflows.
	 *
	 * @return this integer's value as a {@code long}
	 * @throws ArithmeticException if the value cannot be represented as a {@code long}
	 */
	public abstract long longValueExact();

	/**
	 * Get this integer's value as an {@code int},
	 * checking for overflows.
	 *
	 * @return this integer's value as an {@code int}
	 * @throws ArithmeticException if the value cannot be represented as a {@code int}
	 */
	public abstract int intValueExact();

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		} else if (!(other instanceof AIntegerPrologTerm)) {
			return false;
		}
		return this.getValue().equals(((AIntegerPrologTerm) other).getValue());
	}

	@Override
	public int hashCode() {
		return this.getValue().hashCode();
	}
}

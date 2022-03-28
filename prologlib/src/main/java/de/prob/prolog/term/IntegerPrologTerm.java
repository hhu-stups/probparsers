/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import java.math.BigInteger;

import de.prob.prolog.output.IPrologTermOutput;

/**
 * Represents a Prolog integer.
 */
public final class IntegerPrologTerm extends AIntegerPrologTerm {
	private final BigInteger value;
	private final long ivalue; // holds the integer if value==null
	// ideally we should create two instance classes, one for long and one for BigInteger

	public IntegerPrologTerm(final BigInteger value) {
		// super(value.toString());
		// TODO: we could check longValueExact and catch ArithmeticException
		this.value = value;
		this.ivalue = -1;
	}

	public IntegerPrologTerm(final long value) {
		//this(BigInteger.valueOf(value));
		this.value = null; // BigInteger is not required
		this.ivalue = value;
	}
	
	public IntegerPrologTerm(final byte[] arr) {
		// super(new BigInteger(arr).toString());
		this.value = new BigInteger(arr);
		this.ivalue = -1;
	}

	
	@Override
	public String getFunctor() {
		if (value==null)
			return Long.toString(ivalue);
		else
			return value.toString();
	}

	@Override
	public BigInteger getValue() {
		if (value==null)
			return BigInteger.valueOf(ivalue);
		else
			return value;
	}

	@Override
	public void toTermOutput(final IPrologTermOutput pto) {
		if (value==null)
			pto.printNumber(ivalue);
		else
			pto.printNumber(value);
	}


	@Override
	public int hashCode() {
		if (value==null)
			return Long.hashCode(ivalue) * 11 + 4;
		else
			return value.hashCode() * 11 + 4;
	}

}

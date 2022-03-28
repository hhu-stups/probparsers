/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import java.math.BigInteger;

import de.prob.prolog.output.IPrologTermOutput;

/**
 * Represents a Prolog integer which can be represented as long.
 * a variation of IntegerPrologTerm which avoids using a BigInteger reference
 */
public final class IntegerLongPrologTerm extends AIntegerPrologTerm {
	protected final long ivalue; // holds the integer 

	public IntegerLongPrologTerm(final long value) {
		this.ivalue = value;
	}
	
	@Override
	public String getFunctor() {
		return Long.toString(ivalue);
	}
	
	@Override
	public BigInteger getValue() {
		return BigInteger.valueOf(ivalue);
	}
	
	@Override
	public long longValueExact() {
		return ivalue;
	}

	@Override
	public int intValueExact() {
		if (this.ivalue > Integer.MAX_VALUE || this.ivalue < Integer.MIN_VALUE) {
			throw new ArithmeticException("IntegerLongPrologTerm value out of int range");
		}
		return (int)this.ivalue;
	}

	@Override
	public void toTermOutput(final IPrologTermOutput pto) {
		pto.printNumber(ivalue);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(ivalue) * 11 + 4;
	}

}

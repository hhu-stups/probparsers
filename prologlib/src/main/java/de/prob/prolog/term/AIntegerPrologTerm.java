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
public abstract class AIntegerPrologTerm extends PrologTerm {

	@Override
	public boolean isNumber() {
		return true;
	}
	

	public BigInteger getValue() {
			throw new IllegalArgumentException("getValue not implemented ");
	}


	@Override
	public boolean equals(final Object other) {
		boolean isEqual;
		if (this == other) {
			isEqual = true;
		} else if (other != null && other instanceof AIntegerPrologTerm) {
			isEqual = this.getValue().equals(((AIntegerPrologTerm) other).getValue());
		} else {
			isEqual = false;
		}
		return isEqual;
	}


}

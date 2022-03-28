/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import java.math.BigInteger;

/**
 * Represents a Prolog integer which can be represented as long.
 * a variation of IntegerPrologTerm which avoids using a BigInteger reference
 */
public abstract class AIntegerPrologTerm extends PrologTerm {

	@Override
	public boolean isNumber() {
		return true;
	}

	public abstract BigInteger getValue();

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		} else if (!(other instanceof AIntegerPrologTerm)) {
			return false;
		}
		return this.getValue().equals(((AIntegerPrologTerm) other).getValue());
	}


}

/*
 * (c) 2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import de.prob.prolog.output.IPrologTermOutput;

/**
 * Represents a Prolog floating-point number (double precision).
 */
public final class FloatPrologTerm extends PrologTerm {

	private final double value;

	public FloatPrologTerm(final double value) {
		this.value = value;
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public boolean isFloat() {
		return true;
	}

	public double getValue() {
		return this.value;
	}

	@Override
	public String getFunctor() {
		// these representations come from SWI/ECLiPSe
		// SICStus does not support non-finite floats at all
		if (Double.isNaN(this.value)) {
			return "1.5NaN";
		} else if (this.value == Double.POSITIVE_INFINITY) {
			return "1.0Inf";
		} else if (this.value == Double.NEGATIVE_INFINITY) {
			return "-1.0Inf";
		} else {
			return Double.toString(this.value);
		}
	}

	@Override
	public void toTermOutput(final IPrologTermOutput pto) {
		pto.printNumber(this.value);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof FloatPrologTerm)) {
			return false;
		}
		return Double.compare(this.value, ((FloatPrologTerm) obj).value) == 0;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(this.value);
	}
}

/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import java.util.ArrayList;
import java.util.List;

import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;

/**
 * This is the abstract base class for Prolog terms
 */
public abstract class PrologTerm {
	// protected final String functor;
	//protected final PrologTerm[] arguments;

	//public PrologTerm(final String functor, final PrologTerm... arguments) {
		//if (functor == null)
		//	throw new IllegalArgumentException("Functor must not be null");
		//this.functor = functor;
		//if (arguments == null || arguments.length == 0) {
		//	this.arguments = null;
		//} else {
		//	this.arguments = arguments;
		//}
	//}

	public boolean isTerm() {
		return false;
	}

	public boolean isAtom() {
		return false;
	}

	public boolean isList() {
		return false;
	}

	public boolean isNumber() {
		return false;
	}

	public boolean isVariable() {
		return false;
	}

	public boolean hasFunctor(final String functor, final int arity) {
		return false;
	}

	public abstract void toTermOutput(IPrologTermOutput pto);

	@Override
	public String toString() {
		PrologTermStringOutput pto = new PrologTermStringOutput();
		toTermOutput(pto);
		return pto.toString();
	}

	public abstract String getFunctor();

	public int getArity() {
		return 0;
		//return arguments == null ? 0 : arguments.length;
	}

	/**
	 * Gets an argument by its index. Note, that numbering starts with 1
	 * 
	 * @param index
	 *            the index of the argument
	 * @return the PrologTerm
	 */
	public PrologTerm getArgument(final int index) {
		throw new IndexOutOfBoundsException("Atom has no arguments");
		//if (arguments == null)
		//	throw new IndexOutOfBoundsException("Atom has no arguments");
		//else
		//	return arguments[index - 1];
	}

    // this should actually have been called atomString; we should rename this to atomGetString
	public static String atomicString(final PrologTerm term) {
		if (term.isAtom())
			return term.getFunctor();
		else
			throw new IllegalArgumentException("Expected a Prolog atom, but was " + term.toString());
	}
	
	// a variation of the above which also accepts numbers
	public static String atomicGetString(final PrologTerm term) {
		if (term.isAtom() || term.isNumber())
			return term.getFunctor();
		else
			throw new IllegalArgumentException("Expected an atomic Prolog term, but was " + term.toString());
	}

	public static List<String> atomicStrings(final Iterable<PrologTerm> terms) {
		List<String> result = new ArrayList<String>();
		for (final PrologTerm term : terms) {
			result.add(atomicString(term));
		}
		return result;
	}

}

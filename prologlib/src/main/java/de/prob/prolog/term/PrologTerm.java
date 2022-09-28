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

	public boolean isAtomic() {
		return this.getArity() == 0 && !this.isVariable();
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

	/**
	 * Check that this term is an atom (0-argument compound term) and return its value (functor) as a string.
	 * 
	 * @return the atom's functor as a string
	 * @throws IllegalArgumentException if this term is not an atom
	 */
	public String atomToString() {
		if (this.isAtom()) {
			return this.getFunctor();
		} else {
			throw new IllegalArgumentException("Expected a Prolog atom, but was " + this + " (" + this.getClass() + ")");
		}
	}

	/**
	 * Shorthand for {@link #atomToString()} on all terms in a list.
	 * 
	 * @param terms the atoms to convert to strings
	 * @return the atoms' functors as strings
	 * @throws IllegalArgumentException if any term in the list is not an atom
	 */
	public static List<String> atomsToStrings(final Iterable<? extends PrologTerm> terms) {
		final List<String> result = new ArrayList<>();
		for (final PrologTerm term : terms) {
			result.add(term.atomToString());
		}
		return result;
	}

	/**
	 * Check that this term is atomic (non-variable and has no arguments) and return its value (functor) as a string.
	 * 
	 * @return the term's functor as a string
	 * @throws IllegalArgumentException if this term is not atomic
	 */
	public String atomicToString() {
		if (this.isAtomic()) {
			return this.getFunctor();
		} else {
			throw new IllegalArgumentException("Expected an atomic Prolog term, but was " + this + " (" + this.getClass() + ")");
		}
	}

	/**
	 * Shorthand for {@link #atomicToString()} on all terms in a list.
	 * 
	 * @param terms the terms to convert to strings
	 * @return the terms' functors as strings
	 * @throws IllegalArgumentException if any term in the list is not atomic
	 */
	public static List<String> atomicsToStrings(final Iterable<? extends PrologTerm> terms) {
		final List<String> result = new ArrayList<>();
		for (final PrologTerm term : terms) {
			result.add(term.atomicToString());
		}
		return result;
	}

	/**
	 * Identical to {@link #atomToString()}.
	 * This method is badly named - it only accepts atoms and not other atomic terms (like numbers).
	 * 
	 * @param term the atom to convert to a string
	 * @return the atom's functor as a string
	 * @throws IllegalArgumentException if this term is not an atom
	 * @deprecated Use {@link #atomToString()} or {@link #atomicToString()} instead.
	 */
	@Deprecated
	public static String atomicString(final PrologTerm term) {
		return term.atomToString();
	}

	/**
	 * Identical to {@link #atomsToStrings(Iterable)}.
	 * This method is badly named - it only accepts atoms and not other atomic terms (like numbers).
	 * 
	 * @param terms the terms to convert to strings
	 * @return the atoms' functors as strings
	 * @throws IllegalArgumentException if this term is not an atom
	 * @deprecated Use {@link #atomsToStrings(Iterable)} or {@link #atomicsToStrings(Iterable)} instead.
	 */
	@Deprecated
	public static List<String> atomicStrings(final Iterable<PrologTerm> terms) {
		return atomsToStrings(terms);
	}

}

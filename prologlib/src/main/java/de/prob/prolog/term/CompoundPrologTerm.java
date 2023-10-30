/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import de.prob.prolog.output.IPrologTermOutput;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a prolog term that consists of a functor and an (optional) list of
 * arguments. If no arguments are given, the term is an atom.
 */
public final class CompoundPrologTerm extends PrologTerm {

	private final String functor;
	private final PrologTerm[] arguments;

	public CompoundPrologTerm(final String functor, final PrologTerm... arguments) {
		this.functor = Objects.requireNonNull(functor, "functor");
		this.arguments = arguments != null && arguments.length > 0 ? arguments : null;
	}

	public CompoundPrologTerm(final String atom) {
		this(atom, (PrologTerm[]) null);
	}

	public static CompoundPrologTerm fromCollection(final String functor, final Collection<? extends PrologTerm> arguments) {
		PrologTerm[] arr = arguments != null && !arguments.isEmpty() ? arguments.toArray(new PrologTerm[0]) : null;
		return new CompoundPrologTerm(functor, arr);
	}

	@Override
	public String getFunctor() {
		return functor;
	}

	@Override
	public int getArity() {
		return arguments == null ? 0 : arguments.length;
	}

	@Override
	public boolean isAtom() {
		return arguments == null || arguments.length == 0;
	}

	@Override
	public boolean isTerm() {
		return true;
	}

	@Override
	public PrologTerm getArgument(final int index) {
		if (arguments == null || arguments.length == 0) {
			throw new IndexOutOfBoundsException("Atom " + functor + " has no arguments");
		} else {
			return arguments[index - 1];
		}
	}

	@Override
	public void toTermOutput(final IPrologTermOutput pto) {
		pto.openTerm(functor);
		if (arguments != null) {
			for (PrologTerm argument : arguments) {
				argument.toTermOutput(pto);
			}
		}
		pto.closeTerm();
	}

	@Override
	public boolean hasFunctor(final String functor) {
		return this.functor.equals(functor);
	}

	@Override
	public boolean hasFunctor(final String functor, final int arity) {
		return this.functor.equals(functor) && getArity() == arity;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof CompoundPrologTerm)) {
			// Note: this will not consider the atom "[]" to be equal to the empty list
			// But then: comparing longer lists with equivalent compound terms would require quite a bit of code
			return false;
		}
		CompoundPrologTerm other = (CompoundPrologTerm) obj;
		return functor.equals(other.functor) && (
			Arrays.equals(arguments, other.arguments)
				|| arguments == null && other.arguments.length == 0
				|| arguments != null && arguments.length == 0 && other.arguments == null
		);
	}

	@Override
	public int hashCode() {
		return 31 * functor.hashCode() + Arrays.hashCode(arguments);
	}
}

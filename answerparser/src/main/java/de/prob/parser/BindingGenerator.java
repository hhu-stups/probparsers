/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.prob.core.sablecc.node.Start;
import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

/**
 * Takes a Prolog term of the form (only its canonical form) "[x=a,y=b,z=c]" and
 * converts it into a mapping x-&gt;a, y-&gt;b, z-&gt;c, where x,y and z are
 * identifiers and a,b,c prolog terms.
 */
public final class BindingGenerator {

	private BindingGenerator() {
	}

	/**
	 * @deprecated This method doesn't distinguish between callback/progress results and the actual final result.
	 * Use {@link #createBinding(PrologTerm)} instead,
	 * or use {@link #createBindingMustNotFail(String, Start)} if you don't want to handle callback/progress/error
	 * results.
	 */
	@Deprecated
	public static Map<String, PrologTerm> createBinding(final Start ast) {
		PrologTerm term = PrologTermGenerator.toPrologTerm(ast);
		return createBinding(term);
	}

	public static Map<String, PrologTerm> createBindingMustNotFail(final String query, final Start ast) {
		PrologTerm term = PrologTermGenerator.toPrologTermMustNotFail(query, ast);
		return createBinding(term);
	}

	public static Map<String, PrologTerm> createBinding(final PrologTerm term) {
		Map<String, PrologTerm> result;
		if (term == null) {
			result = null;
		} else if (term.isList()) {
			result = createBinding((ListPrologTerm) term);
		} else {
			throw new IllegalArgumentException("Expected Prolog list, but was " + term);
		}
		return result;
	}

	private static Map<String, PrologTerm> createBinding(final ListPrologTerm list) {
		Map<String, PrologTerm> result = new HashMap<>();
		for (PrologTerm element : list) {
			CompoundPrologTerm binding = getCompoundTerm(element, "=", 2);
			result.put(
					binding.getArgument(1).atomToString(),
					binding.getArgument(2)
			);
		}
		return Collections.unmodifiableMap(result);
	}

	public static CompoundPrologTerm getCompoundTerm(final PrologTerm term, final String functor) {
		return checkFunctor(checkComponentType(term), functor);
	}

	public static CompoundPrologTerm getCompoundTerm(final PrologTerm term, final int arity) {
		return checkArity(checkComponentType(term), arity);
	}

	public static CompoundPrologTerm getCompoundTerm(final PrologTerm term, final String functor, final int arity) {
		return checkSignature(checkComponentType(term), functor, arity);
	}

	private static CompoundPrologTerm checkComponentType(final PrologTerm term) {
		if (!(term instanceof CompoundPrologTerm)) {
			final String message = "Expected CompoundPrologTerm, but got " + term.getClass().getSimpleName();
			throw new ResultParserException(message);
		}
		return (CompoundPrologTerm) term;
	}

	public static CompoundPrologTerm getCompoundTerm(final Map<String, PrologTerm> bindings, final String name, final String functor, final int arity) {
		final PrologTerm prologTerm = getFromBindings(bindings, name);
		return getCompoundTerm(prologTerm, functor, arity);
	}

	public static CompoundPrologTerm getCompoundTerm(final Map<String, PrologTerm> bindings, final String name, final int arity) {
		final PrologTerm prologTerm = getFromBindings(bindings, name);
		return getCompoundTerm(prologTerm, arity);
	}

	private static CompoundPrologTerm checkSignature(final CompoundPrologTerm term, final String functor, final int arity) {
		checkArity(term, arity);
		checkFunctor(term, functor);
		return term;
	}

	private static CompoundPrologTerm checkFunctor(final CompoundPrologTerm term, final String functor) {
		if (!term.getFunctor().equals(functor)) {
			final String message =
				"Expected " + term + " to have functor " + functor + ", but got " + term.getFunctor();
			throw new ResultParserException(message);
		}
		return term;
	}

	private static CompoundPrologTerm checkArity(final CompoundPrologTerm term, final int arity) {
		if (term.getArity() != arity) {
			final String message = "Expected " + term + " to have an arity " + arity + ", but got " + term.getArity();
			throw new ResultParserException(message);
		}
		return term;
	}

	public static ListPrologTerm getList(final PrologTerm term) {
		if (term instanceof ListPrologTerm) {
			return (ListPrologTerm) term;
		}
		final String message = "Expected ListPrologTerm, but got " + term.getClass().getSimpleName();
		throw new ResultParserException(message);
	}

	public static ListPrologTerm getList(final Map<String, PrologTerm> bindings, final String name) {
		PrologTerm prologTerm = getFromBindings(bindings, name);
		return getList(prologTerm);
	}

	public static ListPrologTerm getList(final ISimplifiedROMap<String, PrologTerm> bindings, final String name) {
		PrologTerm prologTerm = getFromBindings(bindings, name);
		return getList(prologTerm);
	}

	public static AIntegerPrologTerm getAInteger(final PrologTerm term) {
		if (term instanceof AIntegerPrologTerm) {
			return (AIntegerPrologTerm) term;
		}
		final String message = "Expected AIntegerPrologTerm, but got " + term.getClass().getSimpleName();
		throw new ResultParserException(message);
	}

	public static AIntegerPrologTerm getAInteger(final Map<String, PrologTerm> bindings, final String name) {
		PrologTerm prologTerm = getFromBindings(bindings, name);
		return getAInteger(prologTerm);
	}

	/**
	 * @deprecated use {@link BindingGenerator#getAInteger(PrologTerm)} instead
	 */
	@Deprecated
	public static IntegerPrologTerm getInteger(final PrologTerm term) {
		if (term instanceof IntegerPrologTerm) {
			return (IntegerPrologTerm) term;
		} else if (term instanceof AIntegerPrologTerm) {
			return new IntegerPrologTerm(((AIntegerPrologTerm) term).getValue());
		}
		final String message = "Expected IntegerPrologTerm, but got " + term.getClass().getSimpleName();
		throw new ResultParserException(message);
	}

	/**
	 * @deprecated use {@link BindingGenerator#getAInteger(Map, String)} instead
	 */
	@Deprecated
	public static IntegerPrologTerm getInteger(final Map<String, PrologTerm> bindings, final String name) {
		PrologTerm prologTerm = getFromBindings(bindings, name);
		return getInteger(prologTerm);
	}

	public static VariablePrologTerm getVariable(final PrologTerm term) {
		if (term instanceof VariablePrologTerm) {
			return (VariablePrologTerm) term;
		}
		final String message = "Expected VariablePrologTerm, but got " + term.getClass().getSimpleName();
		throw new ResultParserException(message);
	}

	public static VariablePrologTerm getVariable(final Map<String, PrologTerm> bindings, final String name) {
		PrologTerm prologTerm = getFromBindings(bindings, name);
		return getVariable(prologTerm);
	}

	private static PrologTerm getFromBindings(final Map<String, PrologTerm> bindings, final String name) {
		PrologTerm prologTerm = bindings.get(name);
		if (prologTerm == null) {
			final String message = "Cannot extract " + name + " from bindings.\n" + listBindings(bindings);
			throw new ResultParserException(message);
		}
		return prologTerm;
	}

	private static PrologTerm getFromBindings(final ISimplifiedROMap<String, PrologTerm> bindings, final String name) {
		PrologTerm prologTerm = bindings.get(name);
		if (prologTerm == null) {
			final String message = "Cannot extract " + name + " from bindings.\n";
			throw new ResultParserException(message);
		}
		return prologTerm;
	}

	private static String listBindings(final Map<String, PrologTerm> bindings) {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, PrologTerm> e : bindings.entrySet()) {
			sb.append(e.getKey());
			sb.append(" -> ");
			sb.append(e.getValue().toString());
			sb.append('\n');
		}
		return sb.toString();
	}
}

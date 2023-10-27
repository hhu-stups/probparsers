package de.prob.prolog.match;

import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.Arrays;
import java.util.Map;

/**
 * Matches on compound Prolog terms. The found compound term can be directly
 * accessed.
 *
 * @see CompoundPrologTerm
 */
public final class PrologTermMatch extends PrologMatch {

	private final String functor;
	private final int arity;
	private final PrologMatch[] args;

	/**
	 * Matches on a term with given functor and arguments
	 *
	 * @param functor the functor, <code>null</code> if it should not be checked
	 * @param arity   the arity, &lt;0 if it should not be checked
	 * @param args    the arguments. If <code>null</code>, they remain unchecked. If
	 *                an element of <code>args</code> is <code>null</code>, it
	 *                remains unchecked.
	 */
	private PrologTermMatch(String name, String functor, int arity, PrologMatch[] args) {
		super(name);
		if (args != null && args.length != arity) {
			throw new IllegalArgumentException("wanted arity is inconsistent with wanted args");
		}

		this.functor = functor;
		this.arity = arity;
		this.args = args != null ? Arrays.copyOf(args, args.length) : null;
	}

	public static PrologTermMatch anonTerm() {
		return namedTerm(null);
	}

	public static PrologTermMatch anonTerm(String functor) {
		return namedTerm(null, functor);
	}

	public static PrologTermMatch anonTerm(String functor, PrologMatch... args) {
		return namedTerm(null, functor, args);
	}

	public static PrologTermMatch anonTerm(String functor, int arity) {
		return namedTerm(null, functor, arity);
	}

	public static PrologTermMatch anonAtom(String functor) {
		return namedAtom(null, functor);
	}

	public static PrologTermMatch namedTerm(String name) {
		return namedTerm(name, null);
	}

	public static PrologTermMatch namedTerm(String name, String functor) {
		return namedTerm(name, functor, -1);
	}

	public static PrologTermMatch namedTerm(String name, String functor, PrologMatch... args) {
		return new PrologTermMatch(name, functor, args != null ? args.length : -1, args);
	}

	public static PrologTermMatch namedTerm(String name, String functor, int arity) {
		return new PrologTermMatch(name, functor, arity, null);
	}

	public static PrologTermMatch namedAtom(String name, String functor) {
		return namedTerm(name, functor, 0);
	}

	@Override
	protected boolean isMatch(PrologTerm term, Map<String, PrologTerm> hits) {
		boolean match = term instanceof CompoundPrologTerm;
		if (match
			&& (arity < 0 || term.getArity() == arity)
			&& (functor == null || functor.equals(term.getFunctor()))) {
			match = args == null || allArgsMatch((CompoundPrologTerm) term, hits);
		}
		return match;
	}

	private boolean allArgsMatch(CompoundPrologTerm term, Map<String, PrologTerm> hits) {
		for (int i = 0; i < arity; i++) {
			PrologMatch argMatch = args[i];
			if (argMatch != null && !argMatch.matches(term.getArgument(i + 1), hits)) {
				return false;
			}
		}
		return true;
	}
}

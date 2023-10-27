package de.prob.prolog.match;

import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.Arrays;
import java.util.Map;

/**
 * Matches on a list with optional given length, provides direct access to the
 * list.
 */
public final class PrologListMatch extends PrologMatch {

	private final int size;
	private final PrologMatch[] args;

	private PrologListMatch(final String name, final int size, PrologMatch[] args) {
		super(name);
		if (args != null && args.length != size) {
			throw new IllegalArgumentException("wanted size is inconsistent with wanted args");
		}

		this.size = size;
		this.args = args != null ? Arrays.copyOf(args, args.length) : null;
	}

	public static PrologListMatch anonList() {
		return namedList(null);
	}

	public static PrologListMatch anonList(int size) {
		return namedList(null, size);
	}

	public static PrologListMatch anonList(PrologMatch... args) {
		return namedList(null, args);
	}

	public static PrologListMatch anonEmptyList() {
		return namedEmptyList(null);
	}

	public static PrologListMatch namedList(String name) {
		return namedList(name, -1);
	}

	public static PrologListMatch namedList(String name, int size) {
		return new PrologListMatch(name, size, null);
	}

	public static PrologListMatch namedList(String name, PrologMatch... args) {
		return new PrologListMatch(name, args != null ? args.length : -1, args);
	}

	public static PrologListMatch namedEmptyList(String name) {
		return new PrologListMatch(name, 0, null);
	}

	@Override
	protected boolean isMatch(PrologTerm term, Map<String, PrologTerm> hits) {
		boolean match = term instanceof ListPrologTerm;
		if (match
			&& (size < 0 || ((ListPrologTerm) term).size() == size)) {
			match = args == null || allArgsMatch((ListPrologTerm) term, hits);
		}
		return match;
	}

	private boolean allArgsMatch(ListPrologTerm term, Map<String, PrologTerm> hits) {
		for (int i = 0; i < size; i++) {
			PrologMatch argMatch = args[i];
			if (argMatch != null && !argMatch.matches(term.get(i), hits)) {
				return false;
			}
		}
		return true;
	}
}

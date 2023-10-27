package de.prob.prolog.match;

import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Matches on a list with optional given length, provides direct access to the
 * list.
 * Also supports destructuring.
 */
public final class PrologDestructedListMatch extends PrologMatch {

	private final int headSize;
	private final PrologMatch[] headElements;
	private final PrologMatch tail;

	PrologDestructedListMatch(final String name, int headSize, PrologMatch[] headElements, PrologMatch tail) {
		super(name);
		if (headElements != null && headElements.length != headSize) {
			throw new IllegalArgumentException("wanted head size is inconsistent with wanted head");
		} else if (headSize < 1) {
			throw new IllegalArgumentException("need at least 1 head element for destructuring");
		}

		this.headSize = headSize;
		this.headElements = headElements != null ? Arrays.copyOf(headElements, headElements.length) : null;
		this.tail = Objects.requireNonNull(tail, "tail");
	}

	@Override
	protected boolean isMatch(PrologTerm term, Map<String, PrologTerm> hits) {
		if (term instanceof ListPrologTerm) {
			ListPrologTerm list = (ListPrologTerm) term;
			return list.size() >= headSize
				&& (headElements == null || headMatch(list, hits))
				&& tail.matches(list.tail(headSize), hits);
		}
		return false;
	}

	private boolean headMatch(ListPrologTerm term, Map<String, PrologTerm> hits) {
		for (int i = 0; i < headElements.length; i++) {
			PrologMatch headElementMatch = headElements[i];
			if (headElementMatch != null && !headElementMatch.matches(term.get(i), hits)) {
				return false;
			}
		}
		return true;
	}
}

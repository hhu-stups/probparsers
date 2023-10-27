package de.prob.prolog.match;

import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Matches on a list with optional given length, provides direct access to the
 * list.
 */
public final class PrologListMatch extends PrologMatch {

	private final int size;
	private final PrologMatch[] elements;

	private PrologListMatch(final String name, final int size, PrologMatch[] elements) {
		super(name);
		if (elements != null && elements.length != size) {
			throw new IllegalArgumentException("wanted size is inconsistent with wanted elements");
		}

		this.size = size;
		this.elements = elements != null ? Arrays.copyOf(elements, elements.length) : null;
	}

	public static PrologListMatch anonList() {
		return namedList(null);
	}

	public static PrologListMatch anonList(int size) {
		return namedList(null, size);
	}

	public static PrologListMatch anonList(PrologMatch... elements) {
		return namedList(null, elements);
	}

	public static PrologListMatch anonEmptyList() {
		return namedEmptyList(null);
	}

	public static PrologDestructedListMatch anonDestructuredList(int headSize, PrologMatch tail) {
		return namedDestructuredList(null, headSize, tail);
	}

	public static PrologDestructedListMatch anonDestructuredList(PrologMatch[] headElements, PrologMatch tail) {
		return namedDestructuredList(null, headElements, tail);
	}

	public static PrologListMatch namedList(String name) {
		return namedList(name, -1);
	}

	public static PrologListMatch namedList(String name, int size) {
		return new PrologListMatch(name, size, null);
	}

	public static PrologListMatch namedList(String name, PrologMatch... elements) {
		return new PrologListMatch(name, elements != null ? elements.length : -1, elements);
	}

	public static PrologListMatch namedEmptyList(String name) {
		return new PrologListMatch(name, 0, null);
	}

	public static PrologDestructedListMatch namedDestructuredList(String name, int headSize, PrologMatch tail) {
		return new PrologDestructedListMatch(name, headSize, null, tail);
	}

	public static PrologDestructedListMatch namedDestructuredList(String name, PrologMatch[] headElements,
																  PrologMatch tail) {
		Objects.requireNonNull(headElements, "headElements");
		return new PrologDestructedListMatch(name, headElements.length, headElements, tail);
	}

	@Override
	protected boolean isMatch(PrologTerm term, Map<String, PrologTerm> hits) {
		if (term instanceof ListPrologTerm) {
			ListPrologTerm list = (ListPrologTerm) term;
			if (size < 0 || list.size() == size) {
				return elements == null || allArgsMatch(list, hits);
			}
		}
		return false;
	}

	private boolean allArgsMatch(ListPrologTerm term, Map<String, PrologTerm> hits) {
		for (int i = 0; i < size; i++) {
			PrologMatch elementMatch = elements[i];
			if (elementMatch != null && !elementMatch.matches(term.get(i), hits)) {
				return false;
			}
		}
		return true;
	}
}

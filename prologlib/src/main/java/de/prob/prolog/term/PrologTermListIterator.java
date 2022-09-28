package de.prob.prolog.term;

import java.util.ListIterator;

class PrologTermListIterator implements ListIterator<PrologTerm> {

	private final PrologTerm[] elements;
	private final int start;
	private final int end;
	private int next;

	public PrologTermListIterator(PrologTerm[] elements, int start, int end) {
		this.elements = elements;
		this.start = start;
		this.end = end;
		this.next = start;
	}

	@Override
	public boolean hasNext() {
		return next < end;
	}

	@Override
	public PrologTerm next() {
		return elements[next++];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(PrologTerm arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPrevious() {
		return next > start;
	}

	@Override
	public int nextIndex() {
		return Math.min(next-start, end);
	}

	@Override
	public PrologTerm previous() {
		return elements[--next];
	}

	@Override
	public int previousIndex() {
		if (next == start)
			return -1;
		return next - 1;
	}

	@Override
	public void set(PrologTerm arg0) {
		throw new UnsupportedOperationException();
	}

}
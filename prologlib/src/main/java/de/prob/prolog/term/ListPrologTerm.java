/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import de.prob.prolog.output.IPrologTermOutput;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents a Prolog list.
 */
public final class ListPrologTerm extends PrologTerm implements List<PrologTerm> {

	private static final ListPrologTerm EMPTY_LIST = new ListPrologTerm(null, 0, 0);

	private final PrologTerm[] elements;
	private final int start;
	private final int end;

	public ListPrologTerm(final PrologTerm... elements) {
		this.elements = elements != null && elements.length > 0 ? elements : null;
		this.start = 0;
		this.end = elements != null ? elements.length : 0;
	}

	private ListPrologTerm(final PrologTerm[] elements, final int start, final int end) {
		this.elements = elements;
		this.start = start;
		this.end = end;
	}

	public static ListPrologTerm fromCollection(final Collection<? extends PrologTerm> elements) {
		if (elements == null || elements.isEmpty()) {
			return EMPTY_LIST;
		}

		return new ListPrologTerm(elements.toArray(new PrologTerm[0]));
	}

	public static ListPrologTerm emptyList() {
		return EMPTY_LIST;
	}

	// Note: this functor and arity are not entirely correct, they don't match the structure of Prolog lists properly.
	// A Prolog list is either the atom [] or a term of the form .(Head, Tail), where Tail is another list.
	// However, we incorrectly use the list's elements as the arguments of the list term, which creates a "flat" list
	// rather than a linked list.
	// For example, the list [1, 2, 3] is incorrectly represented as .(1, 2, 3) rather than .(1, .(2, .(3, []))).
	// This doesn't seem to matter in practice though, nobody uses getArity/getArgument on ListPrologTerms.
	// Constructing a proper linked list structure would be expensive, and nobody would use it, so we'll keep using
	// this somewhat incorrect structure.

	@Override
	public String getFunctor() {
		return isEmpty() ? "[]" : ".";
	}

	@Override
	public int getArity() {
		return size();
	}

	@Override
	public boolean isAtom() {
		// The empty list is an atom in Prolog.
		return this.isEmpty();
	}

	@Override
	public boolean isList() {
		return true;
	}

	@Override
	public PrologTerm getArgument(final int index) {
		if (isEmpty()) {
			throw new IndexOutOfBoundsException("List has no arguments");
		} else {
			return get(index - 1);
		}
	}

	@Override
	public int size() {
		return end - start;
	}

	@Override
	public PrologTerm get(final int index) {
		int i = index + start;
		if (i < start || i >= end) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return elements[i];
	}

	@Override
	public void toTermOutput(final IPrologTermOutput pto) {
		if (isEmpty()) {
			pto.emptyList();
		} else {
			pto.openList();
			for (PrologTerm t : this) {
				t.toTermOutput(pto);
			}
			pto.closeList();
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof List<?>)) {
			return false;
		}

		List<?> other = (List<?>) obj;
		if (size() != other.size()) {
			return false;
		}

		Iterator<PrologTerm> e1 = iterator();
		Iterator<?> e2 = other.iterator();
		while (e1.hasNext() && e2.hasNext()) {
			PrologTerm o1 = e1.next();
			Object o2 = e2.next();
			if (!Objects.equals(o1, o2)) {
				return false;
			}
		}
		return !(e1.hasNext() || e2.hasNext());
	}

	@Override
	public int hashCode() {
		return 31 * Objects.hash(start, end) + Arrays.hashCode(elements);
	}

	@Override
	public Iterator<PrologTerm> iterator() {
		return listIterator();
	}

	@Override
	public boolean add(final PrologTerm o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends PrologTerm> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(final Object o) {
		for (PrologTerm t : this) {
			if (Objects.equals(t, o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean remove(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		if (isEmpty()) {
			return new PrologTerm[0];
		} else {
			return Arrays.copyOfRange(elements, start, end);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int size = size();
		if (a.length < size) {
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
		}
		if (size > 0) {
			System.arraycopy(elements, 0, a, 0, size);
		}
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	@Override
	public void add(final int index, final PrologTerm element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends PrologTerm> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(final Object object) {
		for (int i = 0, size = size(); i < size; i++) {
			if (Objects.equals(get(i), object)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(final Object object) {
		for (int i = size() - 1; i >= 0; i--) {
			if (Objects.equals(get(i), object)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<PrologTerm> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<PrologTerm> listIterator(final int index) {
		if (index < 0 || index > this.size()) {
			throw new IndexOutOfBoundsException();
		}
		return new PrologTermListIterator(index);
	}

	@Override
	public PrologTerm remove(final int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PrologTerm set(final int index, final PrologTerm element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListPrologTerm subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex > this.size() || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException();
		} else if (fromIndex == toIndex) {
			return EMPTY_LIST;
		}
		return new ListPrologTerm(this.elements, this.start + fromIndex, this.start + toIndex);
	}

	public ListPrologTerm tail() {
		return tail(1);
	}

	public ListPrologTerm tail(int start) {
		if (start == 0) {
			return this;
		} else if (start < 0) {
			throw new IllegalArgumentException("start must be non-negative");
		}

		int size = size();
		if (size < start) {
			throw new IllegalStateException("Cannot call tail on an empty list");
		} else if (size == start) {
			return EMPTY_LIST;
		}
		return new ListPrologTerm(this.elements, this.start + start, this.end);
	}

	public PrologTerm head() {
		if (isEmpty()) {
			throw new IllegalStateException("Cannot call head on an empty list");
		}
		return get(0);
	}

	private final class PrologTermListIterator implements ListIterator<PrologTerm> {

		private int cursor;

		PrologTermListIterator(int cursor) {
			this.cursor = cursor;
		}

		@Override
		public boolean hasNext() {
			return cursor < size();
		}

		@Override
		public PrologTerm next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			PrologTerm term = get(cursor);
			cursor++;
			return term;
		}

		@Override
		public boolean hasPrevious() {
			return cursor > 0;
		}

		@Override
		public PrologTerm previous() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			--cursor;
			return get(cursor);
		}

		@Override
		public int nextIndex() {
			return cursor;
		}

		@Override
		public int previousIndex() {
			return cursor - 1;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(PrologTerm t) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(PrologTerm t) {
			throw new UnsupportedOperationException();
		}
	}
}

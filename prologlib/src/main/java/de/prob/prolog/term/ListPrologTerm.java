/*
 * (c) 2009-2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.prolog.term;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import de.prob.prolog.output.IPrologTermOutput;

/**
 * Represents a Prolog list.
 */
public final class ListPrologTerm extends PrologTerm implements List<PrologTerm> {
	private static final ListPrologTerm EMPTY_LIST = new ListPrologTerm();

	private final PrologTerm[] elements;

	private final int start; // TODO: check if we need these attributes?? We are wasting space
	private final int end;
	

	public ListPrologTerm(final PrologTerm... elements) {
		this.elements = elements;
		this.start = 0;
		this.end = elements.length;
	}

	private ListPrologTerm(final PrologTerm[] elements, final int start, final int end) {
		assert start >= 0 && start <= end && end <= elements.length;
		this.elements = elements;
		this.start = start;
		this.end = end;
	}

	public ListPrologTerm(final Collection<? extends PrologTerm> elements) {
		this(elements.toArray(new PrologTerm[0]));
	}

	public static ListPrologTerm emptyList() {
		return EMPTY_LIST;
	}
	
	// Note: this functor and arity are not entirely correct, they don't match the structure of Prolog lists properly.
	// A Prolog list is either the atom [] or a term of the form .(Head, Tail), where Tail is another list.
	// However, we incorrectly use the list's elements as the arguments of the list term, which creates a "flat" list rather than a linked list.
	// For example, the list [1, 2, 3] is incorrectly represented as .(1, 2, 3) rather than .(1, .(2, .(3, []))).
	// This doesn't seem to matter in practice though, nobody uses getArity/getArgument on ListPrologTerms.
	// Constructing a proper linked list structure would be expensive, and nobody would use it, so we'll keep using this somewhat incorrect structure.
	
	@Override
	public String getFunctor() {
		return (elements.length == 0 ? "[]" : ".");
	}
	
	@Override
	public int getArity() {
		return end;
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
		if (elements == null)
			throw new IndexOutOfBoundsException("List has no arguments");
		else
			return elements[index + start - 1];
	}
	
	@Override
	public int size() {
		return end - start;
	}

	@Override
	public PrologTerm get(final int index) {
		int i = index + start;
		if (i >= end)
			throw new ArrayIndexOutOfBoundsException();
		return elements[i];
	}

	@Override
	public void toTermOutput(final IPrologTermOutput pto) {
		pto.openList();
		for (int i = start; i < end; i++) {
			elements[i].toTermOutput(pto);
		}
		pto.closeList();
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof List<?>)) {
			return false;
		}
		int i = this.start;
		for (final Object o : (List<?>)other) {
			if (i >= this.end || !Objects.equals(this.elements[i], o)) {
				return false;
			}
			i++;
		}
		return i == this.end;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(elements) + start * 13 + end;
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
		for (int i = start; i < end; i++) {
			if (elements[i].equals(o))
				return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		boolean contained = true;
		for (Object o : c) {
			contained &= contains(o);
		}
		return contained;
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
		Object[] res = new Object[size()];
		System.arraycopy(elements, start, res, 0, size());
		return res;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int size = size();
		if (a.length < size) {
			a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		}
		System.arraycopy(elements, 0, a, 0, size);
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
		for (int i = start; i < end; i++) {
			if (elements[i].equals(object))
				return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(final Object object) {
		for (int i = end - 1; i >= start; i--) {
			if (elements[i].equals(object))
				return i;
		}
		return -1;
	}

	@Override
	public ListIterator<PrologTerm> listIterator() {
		return new PrologTermListIterator(elements, start, end);
	}

	@Override
	public ListIterator<PrologTerm> listIterator(final int index) {
		if (index < 0 || index > this.size()) {
			throw new IndexOutOfBoundsException();
		}
		return new PrologTermListIterator(elements, start + index, end);
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
		}
		return new ListPrologTerm(this.elements, this.start + fromIndex, this.start + toIndex);
	}

	public ListPrologTerm tail() {
		if (isEmpty())
			throw new IllegalStateException("Cannot call tail on an empty list");
		if (size() == 1)
			return EMPTY_LIST;
		return new ListPrologTerm(this.elements, start + 1, end);
	}

	public PrologTerm head() {
		return get(0);
	}

}

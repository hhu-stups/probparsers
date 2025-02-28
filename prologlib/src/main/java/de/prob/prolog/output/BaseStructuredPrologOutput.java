package de.prob.prolog.output;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.FloatPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

abstract class BaseStructuredPrologOutput implements IPrologTermOutput {

	protected final Deque<TermBuilder> termBuilderStack = new ArrayDeque<>();

	protected abstract void addFinishedTerm(PrologTerm term);

	protected abstract void fullStopImpl();

	private void addTerm(PrologTerm term) {
		Objects.requireNonNull(term, "term");
		if (this.termBuilderStack.isEmpty()) {
			this.addFinishedTerm(term);
		} else {
			this.termBuilderStack.getFirst().addArgument(term);
		}
	}

	@Override
	public IPrologTermOutput closeList() {
		PrologTerm term = this.termBuilderStack.pop().buildList();
		this.addTerm(term);
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		PrologTerm term = this.termBuilderStack.pop().buildTerm();
		this.addTerm(term);
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		this.termBuilderStack.push(new TermBuilder(null));
		return this;
	}

	@Override
	public IPrologTermOutput tailSeparator() {
		this.termBuilderStack.getFirst().addTailSeparator();
		return this;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor, final boolean ignoreIndentation) {
		this.termBuilderStack.push(new TermBuilder(Objects.requireNonNull(functor, "functor")));
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(final String content) {
		this.addTerm(new CompoundPrologTerm(content));
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final long number) {
		this.addTerm(AIntegerPrologTerm.create(number));
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final BigInteger number) {
		this.addTerm(AIntegerPrologTerm.create(number));
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final double number) {
		this.addTerm(new FloatPrologTerm(number));
		return this;
	}

	@Override
	public IPrologTermOutput printString(final String content) {
		throw new UnsupportedOperationException("Double-quoted string currently cannot be printed as a structured term");
	}

	@Override
	public IPrologTermOutput printVariable(final String var) {
		this.addTerm(new VariablePrologTerm(var));
		return this;
	}

	@Override
	public IPrologTermOutput printTerm(final PrologTerm term) {
		this.addTerm(term);
		return this;
	}

	@Override
	public IPrologTermOutput fullstop() {
		if (!this.termBuilderStack.isEmpty()) {
			throw new IllegalArgumentException(this.termBuilderStack.size() + " unclosed term(s) or list(s)");
		}

		this.fullStopImpl();
		return this.flush();
	}

	public void reset() {
		this.termBuilderStack.clear();
	}

	public boolean isBuildingTerm() {
		return !this.termBuilderStack.isEmpty();
	}

	protected static final class TermBuilder {

		final String functor;
		List<PrologTerm> args;
		int tail;

		TermBuilder(String functor) {
			this.functor = functor;
			this.tail = -1;
		}

		PrologTerm buildList() {
			if (this.functor != null) {
				throw new IllegalStateException("expected list");
			} else if (this.hasTail()) {
				int size = this.args != null ? this.args.size() : 0;
				int before = this.tail;
				int after = size - this.tail;
				if (before < 1 || after != 1) {
					throw new IllegalStateException("need at least one argument before the tail separator and exactly one after");
				}

				// TODO
				throw new UnsupportedOperationException("tail separator not supported");
			}

			return ListPrologTerm.fromCollection(this.args);
		}

		PrologTerm buildTerm() {
			if (this.functor == null) {
				throw new IllegalStateException("expected term");
			} else if (this.hasTail()) {
				throw new IllegalStateException("illegal tail separator in term");
			}

			return CompoundPrologTerm.fromCollection(this.functor, this.args);
		}

		void addArgument(PrologTerm term) {
			if (this.hasTail()) {
				int size = this.args != null ? this.args.size() : 0;
				int after = size - this.tail;
				if (after >= 1) {
					throw new IllegalStateException("cannot add more than one term after tail separator");
				}
			}

			if (this.args == null) {
				this.args = new ArrayList<>();
			}

			this.args.add(term);
		}

		boolean hasTail() {
			return this.tail >= 0;
		}

		void addTailSeparator() {
			if (this.hasTail()) {
				throw new IllegalStateException("cannot add more than one tail separator");
			} else if (this.functor != null) {
				throw new IllegalStateException("cannot add tail separator to non-list term");
			} else if (this.args == null || this.args.isEmpty()) {
				throw new IllegalStateException("need at least one element in list before adding tail separator");
			}

			this.tail = this.args.size();
		}
	}
}
